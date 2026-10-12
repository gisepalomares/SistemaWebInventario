/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entidad.DetallePedido;
import entidad.Pedido;
import interfaces.IPedidoDAO;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import util.ConexionBD;
import util.PedidoException;

public class PedidoDAO implements IPedidoDAO {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";   // sin O, 0, I, 1
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int HORAS_RESERVA = 48;

    @Override
    public void registrar(Pedido pedido) throws SQLException, PedidoException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                // Siempre en el mismo orden (por id): evita bloqueos cruzados entre dos pedidos simultáneos
                List<DetallePedido> detalles = new ArrayList<>(pedido.getDetalles());
                Collections.sort(detalles, new Comparator<DetallePedido>() {
                    @Override
                    public int compare(DetallePedido a, DetallePedido b) {
                        return Integer.compare(a.getIdProducto(), b.getIdProducto());
                    }
                });

                // 1) Reservar stock y leer el precio real desde la BD
                BigDecimal total = BigDecimal.ZERO;
                for (DetallePedido d : detalles) {
                    reservarStock(cn, d);
                    leerProducto(cn, d);
                    total = total.add(d.getSubtotal());
                }
                pedido.setTotal(total);

                // 2) Cabecera del pedido
                pedido.setCodigoRecojo(codigoUnico(cn));
                boolean enTienda = Pedido.METODO_EN_TIENDA.equals(pedido.getMetodoPago());
                // El vencimiento se calcula con el reloj de MySQL (el mismo de fecha_pedido y de NOW())
                String sqlPedido = "INSERT INTO pedido (estado, codigo_recojo, fecha_vencimiento, metodo_pago, total, id_cliente) "
                        + "VALUES (?, ?, "
                        + (enTienda ? "DATE_ADD(NOW(), INTERVAL " + HORAS_RESERVA + " HOUR)" : "NULL")
                        + ", ?, ?, ?)";
                try (PreparedStatement ps = cn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, pedido.getEstado());
                    ps.setString(2, pedido.getCodigoRecojo());
                    ps.setString(3, pedido.getMetodoPago());
                    ps.setBigDecimal(4, total);
                    ps.setInt(5, pedido.getIdCliente());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se obtuvo el id del pedido creado");
                        }
                        pedido.setIdPedido(keys.getInt(1));
                    }
                }

                // 3) Detalle
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)")) {
                    for (DetallePedido d : detalles) {
                        ps.setInt(1, pedido.getIdPedido());
                        ps.setInt(2, d.getIdProducto());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 4) Pago en línea simulado (aprobado): se registra el pago
                if (Pedido.PAGADO.equals(pedido.getEstado()) && total.signum() > 0) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO pago (id_pedido, monto, medio, estado, referencia) "
                            + "VALUES (?, ?, 'SIMULADO', 'APROBADO', ?)")) {
                        ps.setInt(1, pedido.getIdPedido());
                        ps.setBigDecimal(2, total);
                        ps.setString(3, "SIM-" + pedido.getCodigoRecojo());
                        ps.executeUpdate();
                    }
                }

                // 5) Fechas ya formateadas por MySQL (sin conversiones de zona horaria)
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT DATE_FORMAT(fecha_pedido, '%d/%m/%Y %H:%i') AS fp, "
                        + "DATE_FORMAT(fecha_vencimiento, '%d/%m/%Y %H:%i') AS fv "
                        + "FROM pedido WHERE id_pedido = ?")) {
                    ps.setInt(1, pedido.getIdPedido());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            pedido.setFechaPedidoTexto(rs.getString("fp"));
                            pedido.setFechaVencimientoTexto(rs.getString("fv"));
                        }
                    }
                }

                cn.commit();

            } catch (SQLException | PedidoException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Pedido> listarPorUsuario(int idUsuario) throws SQLException {
        // Una sola consulta: cada fila es una línea de detalle y se agrupa por pedido
        String sql = "SELECT p.id_pedido, p.estado, p.codigo_recojo, p.metodo_pago, p.total, p.id_cliente, "
                + "DATE_FORMAT(p.fecha_pedido, '%d/%m/%Y %H:%i') AS fecha_pedido_txt, "
                + "DATE_FORMAT(p.fecha_vencimiento, '%d/%m/%Y %H:%i') AS fecha_venc_txt, "
                + "d.id_producto, pr.nombre AS producto, d.cantidad, d.precio_unitario "
                + "FROM pedido p "
                + "JOIN cliente c ON c.id_cliente = p.id_cliente "
                + "JOIN detalle_pedido d ON d.id_pedido = p.id_pedido "
                + "JOIN producto pr ON pr.id_producto = d.id_producto "
                + "WHERE c.id_usuario = ? "
                + "ORDER BY p.id_pedido DESC, d.id_detalle_pedido";

        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPedido = rs.getInt("id_pedido");
                    Pedido p = pedidos.get(idPedido);
                    if (p == null) {
                        p = new Pedido();
                        p.setIdPedido(idPedido);
                        p.setEstado(rs.getString("estado"));
                        p.setCodigoRecojo(rs.getString("codigo_recojo"));
                        p.setMetodoPago(rs.getString("metodo_pago"));
                        p.setTotal(rs.getBigDecimal("total"));
                        p.setIdCliente(rs.getInt("id_cliente"));
                        p.setFechaPedidoTexto(rs.getString("fecha_pedido_txt"));
                        p.setFechaVencimientoTexto(rs.getString("fecha_venc_txt"));
                        pedidos.put(idPedido, p);
                    }
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    p.getDetalles().add(d);
                }
            }
        }
        return new ArrayList<>(pedidos.values());
    }

    // ==================================================================
    //  ZONA INTERNA (vendedor / administrador)
    // ==================================================================
    @Override
    public List<Pedido> listarGestion(String estado, String texto) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_pedido, p.estado, p.codigo_recojo, p.metodo_pago, p.total, p.id_cliente, "
                + "DATE_FORMAT(p.fecha_pedido, '%d/%m/%Y %H:%i') AS fecha_pedido_txt, "
                + "DATE_FORMAT(p.fecha_vencimiento, '%d/%m/%Y %H:%i') AS fecha_venc_txt, "
                + "c.nombres AS cliente, c.documento, c.telefono, "
                + "d.id_producto, pr.nombre AS producto, d.cantidad, d.precio_unitario "
                + "FROM pedido p "
                + "JOIN cliente c ON c.id_cliente = p.id_cliente "
                + "JOIN detalle_pedido d ON d.id_pedido = p.id_pedido "
                + "JOIN producto pr ON pr.id_producto = d.id_producto "
                + "WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (estado != null && !estado.isEmpty()) {
            sql.append("AND p.estado = ? ");
            params.add(estado);
        }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append("AND (p.codigo_recojo LIKE ? OR c.nombres LIKE ? OR c.documento LIKE ?) ");
            String patron = "%" + texto.trim() + "%";
            params.add(patron);
            params.add(patron);
            params.add(patron);
        }
        sql.append("ORDER BY p.id_pedido DESC, d.id_detalle_pedido");

        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPedido = rs.getInt("id_pedido");
                    Pedido p = pedidos.get(idPedido);
                    if (p == null) {
                        p = new Pedido();
                        p.setIdPedido(idPedido);
                        p.setEstado(rs.getString("estado"));
                        p.setCodigoRecojo(rs.getString("codigo_recojo"));
                        p.setMetodoPago(rs.getString("metodo_pago"));
                        p.setTotal(rs.getBigDecimal("total"));
                        p.setIdCliente(rs.getInt("id_cliente"));
                        p.setFechaPedidoTexto(rs.getString("fecha_pedido_txt"));
                        p.setFechaVencimientoTexto(rs.getString("fecha_venc_txt"));
                        p.setClienteNombre(rs.getString("cliente"));
                        p.setClienteDocumento(rs.getString("documento"));
                        p.setClienteTelefono(rs.getString("telefono"));
                        pedidos.put(idPedido, p);
                    }
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    p.getDetalles().add(d);
                }
            }
        }
        return new ArrayList<>(pedidos.values());
    }

    @Override
    public Map<String, Integer> contarPorEstado() throws SQLException {
        Map<String, Integer> conteos = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement("SELECT estado, COUNT(*) AS n FROM pedido GROUP BY estado"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteos.put(rs.getString("estado"), rs.getInt("n"));
            }
        }
        return conteos;
    }

    @Override
    public void registrarPagoEnTienda(int idPedido, String medio, String referencia)
            throws SQLException, PedidoException {
        vencerPedidos();   // si su plazo ya terminó, primero se vence (y se libera su stock)

        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                FilaPedido f = bloquearPedido(cn, idPedido);
                exigirEstado(f, Pedido.PENDIENTE_PAGO, "registrar el pago");

                if (f.total.signum() > 0) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO pago (id_pedido, monto, medio, estado, referencia) "
                            + "VALUES (?, ?, ?, 'APROBADO', ?)")) {
                        ps.setInt(1, idPedido);
                        ps.setBigDecimal(2, f.total);
                        ps.setString(3, medio);
                        if (referencia == null) {
                            ps.setNull(4, java.sql.Types.VARCHAR);   // efectivo: no hay voucher
                        } else {
                            ps.setString(4, referencia);
                        }
                        ps.executeUpdate();
                    }
                }
                cambiarEstado(cn, idPedido, Pedido.PAGADO);
                cn.commit();
            } catch (SQLException | PedidoException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    @Override
    public void marcarListo(int idPedido) throws SQLException, PedidoException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                FilaPedido f = bloquearPedido(cn, idPedido);
                exigirEstado(f, Pedido.PAGADO, "marcar el pedido como listo");
                cambiarEstado(cn, idPedido, Pedido.LISTO_RECOGER);
                cn.commit();
            } catch (SQLException | PedidoException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    @Override
    public int entregar(int idPedido, int idUsuario, String codigoIngresado) throws SQLException, PedidoException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                FilaPedido f = bloquearPedido(cn, idPedido);
                exigirEstado(f, Pedido.LISTO_RECOGER, "entregar el pedido");
                if (codigoIngresado == null || !f.codigoRecojo.equalsIgnoreCase(codigoIngresado.trim())) {
                    throw new PedidoException("El código de recojo no coincide con el del pedido.");
                }

                // 1) La venta (el pedido se convierte en venta al entregarse)
                int idVenta;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO venta (id_cliente, id_usuario, id_pedido, total, tipo_comprobante) "
                        + "VALUES (?, ?, ?, ?, 'BOLETA')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, f.idCliente);
                    ps.setInt(2, idUsuario);
                    ps.setInt(3, idPedido);
                    ps.setBigDecimal(4, f.total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se obtuvo el id de la venta creada");
                        }
                        idVenta = keys.getInt(1);
                    }
                }

                // 2) Detalle de venta + salida real de stock (sale del stock físico y de lo reservado)
                try (PreparedStatement upd = cn.prepareStatement(
                        "UPDATE producto SET stock_actual = stock_actual - ?, "
                        + "stock_reservado = GREATEST(stock_reservado - ?, 0) "
                        + "WHERE id_producto = ? AND stock_actual >= ?"); PreparedStatement ins = cn.prepareStatement(
                                "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) "
                                + "VALUES (?, ?, ?, ?, ?)")) {

                    for (DetallePedido d : leerDetalles(cn, idPedido)) {
                        upd.setInt(1, d.getCantidad());
                        upd.setInt(2, d.getCantidad());
                        upd.setInt(3, d.getIdProducto());
                        upd.setInt(4, d.getCantidad());
                        if (upd.executeUpdate() == 0) {
                            throw new PedidoException("El stock físico de un producto es menor al del pedido. "
                                    + "Avise al administrador para revisar el inventario.");
                        }
                        ins.setInt(1, idVenta);
                        ins.setInt(2, d.getIdProducto());
                        ins.setInt(3, d.getCantidad());
                        ins.setBigDecimal(4, d.getPrecioUnitario());
                        ins.setBigDecimal(5, d.getSubtotal());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }

                cambiarEstado(cn, idPedido, Pedido.ENTREGADO);
                cn.commit();
                return idVenta;

            } catch (SQLException | PedidoException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    @Override
    public void cancelar(int idPedido) throws SQLException, PedidoException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                FilaPedido f = bloquearPedido(cn, idPedido);
                if (!Pedido.PENDIENTE_PAGO.equals(f.estado) && !Pedido.PAGADO.equals(f.estado)
                        && !Pedido.LISTO_RECOGER.equals(f.estado)) {
                    throw new PedidoException("No se puede cancelar: el pedido está en estado «"
                            + textoEstado(f.estado) + "».");
                }
                liberarReserva(cn, idPedido);
                cambiarEstado(cn, idPedido, Pedido.CANCELADO);
                cn.commit();
            } catch (SQLException | PedidoException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    @Override
    public int vencerPedidos() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(
                "SELECT id_pedido FROM pedido WHERE estado = 'PENDIENTE_PAGO' "
                + "AND fecha_vencimiento IS NOT NULL AND fecha_vencimiento < NOW()"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        int vencidos = 0;
        for (int id : ids) {
            if (vencerUno(id)) {
                vencidos++;
            }
        }
        return vencidos;
    }

    /**
     * Vence un pedido (cada uno en su propia transacción). false si ya no
     * correspondía vencerlo.
     */
    private boolean vencerUno(int idPedido) throws SQLException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                FilaPedido f = bloquearPedido(cn, idPedido);
                // Se vuelve a comprobar con la fila bloqueada: otro proceso pudo pagarlo o cancelarlo
                if (!Pedido.PENDIENTE_PAGO.equals(f.estado) || !f.vencido) {
                    cn.rollback();
                    return false;
                }
                liberarReserva(cn, idPedido);
                cambiarEstado(cn, idPedido, Pedido.VENCIDO);
                cn.commit();
                return true;
            } catch (PedidoException e) {
                cn.rollback();
                return false;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    //  Auxiliares de la zona interna
    // ------------------------------------------------------------------
    /**
     * Datos del pedido leídos con la fila bloqueada (FOR UPDATE).
     */
    private static class FilaPedido {

        String estado;
        String codigoRecojo;
        BigDecimal total;
        int idCliente;
        boolean vencido;
    }

    private FilaPedido bloquearPedido(Connection cn, int idPedido) throws SQLException, PedidoException {
        String sql = "SELECT estado, codigo_recojo, total, id_cliente, "
                + "(fecha_vencimiento IS NOT NULL AND fecha_vencimiento < NOW()) AS vencido "
                + "FROM pedido WHERE id_pedido = ? FOR UPDATE";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new PedidoException("El pedido no existe.");
                }
                FilaPedido f = new FilaPedido();
                f.estado = rs.getString("estado");
                f.codigoRecojo = rs.getString("codigo_recojo");
                f.total = rs.getBigDecimal("total");
                f.idCliente = rs.getInt("id_cliente");
                f.vencido = rs.getBoolean("vencido");
                return f;
            }
        }
    }

    private void exigirEstado(FilaPedido f, String requerido, String accion) throws PedidoException {
        if (!requerido.equals(f.estado)) {
            throw new PedidoException("No se puede " + accion + ": el pedido está en estado «"
                    + textoEstado(f.estado) + "».");
        }
    }

    private String textoEstado(String estado) {
        Pedido p = new Pedido();
        p.setEstado(estado);
        return p.getEstadoTexto();
    }

    private void cambiarEstado(Connection cn, int idPedido, String nuevoEstado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE pedido SET estado = ? WHERE id_pedido = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    private List<DetallePedido> leerDetalles(Connection cn, int idPedido) throws SQLException {
        List<DetallePedido> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT id_producto, cantidad, precio_unitario FROM detalle_pedido "
                + "WHERE id_pedido = ? ORDER BY id_producto")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /**
     * Devuelve al stock disponible lo que el pedido tenía reservado.
     */
    private void liberarReserva(Connection cn, int idPedido) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE producto SET stock_reservado = GREATEST(stock_reservado - ?, 0) WHERE id_producto = ?")) {
            for (DetallePedido d : leerDetalles(cn, idPedido)) {
                ps.setInt(1, d.getCantidad());
                ps.setInt(2, d.getIdProducto());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Reserva el stock con un UPDATE condicionado: solo se aplica si todavía
     * alcanza el disponible (stock_actual - stock_reservado). Si dos clientes
     * compiten por la última unidad, MySQL bloquea la fila y solo uno de los
     * dos UPDATE afecta una fila.
     */
    private void reservarStock(Connection cn, DetallePedido d) throws SQLException, PedidoException {
        String sql = "UPDATE producto SET stock_reservado = stock_reservado + ? "
                + "WHERE id_producto = ? AND estado = 1 AND stock_actual - stock_reservado >= ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, d.getCantidad());
            ps.setInt(2, d.getIdProducto());
            ps.setInt(3, d.getCantidad());
            if (ps.executeUpdate() == 0) {
                throw new PedidoException("No hay stock suficiente de \"" + nombreProducto(cn, d.getIdProducto())
                        + "\". No se reservó nada; revise su carrito.");
            }
        }
    }

    /**
     * Lee nombre y precio vigentes (la fila ya está bloqueada por el UPDATE de
     * la reserva).
     */
    private void leerProducto(Connection cn, DetallePedido d) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT nombre, precio_venta FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, d.getIdProducto());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Producto inexistente: " + d.getIdProducto());
                }
                d.setNombreProducto(rs.getString("nombre"));
                d.setPrecioUnitario(rs.getBigDecimal("precio_venta"));
            }
        }
    }

    private String nombreProducto(Connection cn, int idProducto) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT nombre FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("nombre") : "producto";
            }
        }
    }

    /**
     * Código de recojo corto y legible, p. ej. ES-K7M4PX (único en la tabla).
     */
    private String codigoUnico(Connection cn) throws SQLException {
        while (true) {
            StringBuilder sb = new StringBuilder("ES-");
            for (int i = 0; i < 6; i++) {
                sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
            }
            String codigo = sb.toString();
            try (PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM pedido WHERE codigo_recojo = ?")) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return codigo;
                    }
                }
            }
        }
    }
}
