package dao;

import entidad.Compra;
import entidad.DetalleCompra;
import interfaces.ICompraDAO;
import util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compras a proveedores: cada compra registrada suma stock y cada anulación lo resta.
 *
 * @author Diego
 */
public class CompraDAO implements ICompraDAO {

    private static final String SELECT_COMPRA
            = "SELECT c.id_compra, c.fecha, c.id_proveedor, c.total, c.estado, "
            + "COALESCE(NULLIF(pr.nombre_comercial, ''), pr.razon_social) AS proveedor "
            + "FROM compra c "
            + "INNER JOIN proveedor pr ON c.id_proveedor = pr.id_proveedor ";

    @Override
    public List<Compra> listar() throws SQLException {
        List<Compra> lista = new ArrayList<>();

        String sql = SELECT_COMPRA + "ORDER BY c.fecha DESC, c.id_compra DESC";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCompra(rs));
            }
        }

        return lista;
    }

    @Override
    public Compra buscarPorId(int idCompra) throws SQLException {
        String sql = SELECT_COMPRA + "WHERE c.id_compra = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCompra);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearCompra(rs) : null;
            }
        }
    }

    @Override
    public List<DetalleCompra> listarDetalles(int idCompra) throws SQLException {
        List<DetalleCompra> lista = new ArrayList<>();

        String sql = "SELECT d.*, p.nombre AS nombre_producto "
                + "FROM detalle_compra d "
                + "INNER JOIN producto p ON d.id_producto = p.id_producto "
                + "WHERE d.id_compra = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCompra);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();

                    d.setIdDetalleCompra(rs.getInt("id_detalle_compra"));
                    d.setIdCompra(rs.getInt("id_compra"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));

                    lista.add(d);
                }
            }
        }

        return lista;
    }

    @Override
    public int registrar(Compra compra, List<DetalleCompra> detalles) throws SQLException {

        if (compra == null || compra.getIdProveedor() <= 0
                || detalles == null || detalles.isEmpty()) {
            throw new SQLException("Datos de compra incompletos.");
        }

        Set<Integer> productos = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCompra d : detalles) {
            if (d == null || d.getIdProducto() <= 0
                    || d.getCantidad() <= 0
                    || d.getPrecioUnitario() == null
                    || d.getPrecioUnitario().signum() < 0
                    || !productos.add(d.getIdProducto())) {
                throw new SQLException("Productos, cantidades o precios no válidos o duplicados.");
            }
            total = total.add(d.getSubtotal());
        }

        Connection cn = ConexionBD.conectar();

        try {
            cn.setAutoCommit(false);

            // El proveedor debe existir.
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT 1 FROM proveedor WHERE id_proveedor = ?")) {
                ps.setInt(1, compra.getIdProveedor());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("El proveedor no existe.");
                    }
                }
            }

            int idCompra;

            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO compra (id_proveedor, total, estado) VALUES (?, ?, 'REGISTRADA')",
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, compra.getIdProveedor());
                ps.setBigDecimal(2, total);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se pudo obtener el ID de la compra.");
                    }
                    idCompra = rs.getInt(1);
                }
            }

            String sqlDetalle = "INSERT INTO detalle_compra "
                    + "(id_compra, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

            // Solo se compra mercadería de productos activos.
            String sqlStock = "UPDATE producto SET stock_actual = stock_actual + ? "
                    + "WHERE id_producto = ? AND estado = 1";

            try (PreparedStatement psDetalle = cn.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = cn.prepareStatement(sqlStock)) {

                for (DetalleCompra d : detalles) {
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());

                    if (psStock.executeUpdate() != 1) {
                        throw new SQLException("Producto inexistente o inactivo: " + d.getIdProducto());
                    }

                    psDetalle.setInt(1, idCompra);
                    psDetalle.setInt(2, d.getIdProducto());
                    psDetalle.setInt(3, d.getCantidad());
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario());
                    psDetalle.executeUpdate();
                }
            }

            cn.commit();

            compra.setIdCompra(idCompra);
            compra.setTotal(total);

            return idCompra;

        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            try {
                cn.close();
            } catch (SQLException e) {
                // La operación principal ya terminó.
            }
        }
    }

    @Override
    public void anular(int idCompra) throws SQLException {

        Connection cn = ConexionBD.conectar();

        try {
            cn.setAutoCommit(false);

            // Bloquea la compra para que no se anule dos veces al mismo tiempo.
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT estado FROM compra WHERE id_compra = ? FOR UPDATE")) {
                ps.setInt(1, idCompra);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("La compra no existe.");
                    }
                    if (!"REGISTRADA".equals(rs.getString("estado"))) {
                        throw new SQLException("La compra ya está anulada.");
                    }
                }
            }

            // Se resta lo que ingresó, sin tocar el stock reservado por pedidos en línea.
            String sqlStock = "UPDATE producto SET stock_actual = stock_actual - ? "
                    + "WHERE id_producto = ? AND stock_actual - stock_reservado >= ?";

            try (PreparedStatement psStock = cn.prepareStatement(sqlStock)) {
                for (DetalleCompra d : listarDetalles(cn, idCompra)) {
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.setInt(3, d.getCantidad());

                    if (psStock.executeUpdate() != 1) {
                        throw new SQLException("No hay stock disponible suficiente para anular: "
                                + d.getNombreProducto());
                    }
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE compra SET estado = 'ANULADA' WHERE id_compra = ?")) {
                ps.setInt(1, idCompra);
                ps.executeUpdate();
            }

            cn.commit();

        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            try {
                cn.close();
            } catch (SQLException e) {
                // La operación principal ya terminó.
            }
        }
    }

    /** Detalles leídos dentro de la transacción de anulación. */
    private List<DetalleCompra> listarDetalles(Connection cn, int idCompra) throws SQLException {
        List<DetalleCompra> lista = new ArrayList<>();

        String sql = "SELECT d.id_producto, d.cantidad, p.nombre AS nombre_producto "
                + "FROM detalle_compra d "
                + "INNER JOIN producto p ON d.id_producto = p.id_producto "
                + "WHERE d.id_compra = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setNombreProducto(rs.getString("nombre_producto"));
                    lista.add(d);
                }
            }
        }

        return lista;
    }

    private Compra mapearCompra(ResultSet rs) throws SQLException {
        Compra c = new Compra();

        c.setIdCompra(rs.getInt("id_compra"));
        c.setFecha(rs.getTimestamp("fecha"));
        c.setIdProveedor(rs.getInt("id_proveedor"));
        c.setProveedor(rs.getString("proveedor"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setEstado(rs.getString("estado"));

        return c;
    }
}
