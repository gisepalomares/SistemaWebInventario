/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import entidad.Venta;
import entidad.DetalleVenta;
import interfaces.IVentaDAO;
import util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 *
 * @author Alex
 */
public class VentaDAO implements IVentaDAO {

    @Override
    public List<Venta> listar() throws SQLException {
        List<Venta> lista = new ArrayList<>();

        String sql = "SELECT * FROM venta ORDER BY fecha DESC, id_venta DESC";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearVenta(rs));
            }
        }

        return lista;
    }

    @Override
    public Venta buscarPorId(int idVenta) throws SQLException {
        String sql = "SELECT * FROM venta WHERE id_venta = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearVenta(rs) : null;
            }
        }
    }

    @Override
    public List<DetalleVenta> listarDetalles(int idVenta)
            throws SQLException {

        List<DetalleVenta> lista = new ArrayList<>();

        String sql = "SELECT d.*, p.nombre AS nombre_producto "
        + "FROM detalle_venta d "
        + "INNER JOIN producto p ON d.id_producto = p.id_producto "
        + "WHERE d.id_venta = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();

                    d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                    d.setIdVenta(rs.getInt("id_venta"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));

                    lista.add(d);
                }
            }
        }

        return lista;
    }

    @Override
    public int registrar(Venta venta, List<DetalleVenta> detalles)
            throws SQLException {

        if (venta == null || venta.getIdUsuario() <= 0
                || detalles == null || detalles.isEmpty()) {
            throw new SQLException("Datos de venta incompletos.");
        }

        // Por ahora solo registramos ventas directas.
        if (venta.getIdPedido() != null) {
            throw new SQLException(
                    "Las ventas provenientes de pedidos requieren otro proceso.");
        }

        if (!"BOLETA".equals(venta.getTipoComprobante())
                && !"FACTURA".equals(venta.getTipoComprobante())) {
            throw new SQLException("Tipo de comprobante no válido.");
        }

        Set<Integer> productos = new HashSet<>();

        for (DetalleVenta d : detalles) {
            if (d == null || d.getIdProducto() <= 0
                    || d.getCantidad() <= 0
                    || !productos.add(d.getIdProducto())) {
                throw new SQLException(
                        "Productos o cantidades no válidos o duplicados.");
            }
        }

        Connection cn = ConexionBD.conectar();

        try {
            cn.setAutoCommit(false);

            BigDecimal total = BigDecimal.ZERO;

            String sqlProducto =
                    "SELECT precio_venta, stock_actual, stock_reservado, estado "
                    + "FROM producto WHERE id_producto = ? FOR UPDATE";

            // Consultar precios y bloquear productos antes de vender.
            try (PreparedStatement ps = cn.prepareStatement(sqlProducto)) {

                for (DetalleVenta d : detalles) {
                    ps.setInt(1, d.getIdProducto());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next() || !rs.getBoolean("estado")) {
                            throw new SQLException(
                                    "Producto inexistente o inactivo: "
                                    + d.getIdProducto());
                        }

                        int disponible = rs.getInt("stock_actual")
                                - rs.getInt("stock_reservado");

                        if (disponible < d.getCantidad()) {
                            throw new SQLException(
                                    "Stock insuficiente para el producto: "
                                    + d.getIdProducto());
                        }

                        BigDecimal precio = rs.getBigDecimal("precio_venta");
                        BigDecimal subtotal = precio.multiply(
                                BigDecimal.valueOf(d.getCantidad()));

                        d.setPrecioUnitario(precio);
                        d.setSubtotal(subtotal);

                        total = total.add(subtotal);
                    }
                }
            }

            String sqlVenta =
                    "INSERT INTO venta "
                    + "(id_cliente, id_usuario, id_pedido, total, tipo_comprobante) "
                    + "VALUES (?, ?, NULL, ?, ?)";

            int idVenta;

            try (PreparedStatement ps = cn.prepareStatement(
                    sqlVenta, Statement.RETURN_GENERATED_KEYS)) {

                if (venta.getIdCliente() == null) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, venta.getIdCliente());
                }

                ps.setInt(2, venta.getIdUsuario());
                ps.setBigDecimal(3, total);
                ps.setString(4, venta.getTipoComprobante());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No se pudo obtener el ID de la venta.");
                    }

                    idVenta = rs.getInt(1);
                }
            }

            String sqlDetalle =
                    "INSERT INTO detalle_venta "
                    + "(id_venta, id_producto, cantidad, precio_unitario, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?)";

            String sqlStock =
                    "UPDATE producto SET stock_actual = stock_actual - ? "
                    + "WHERE id_producto = ? "
                    + "AND estado = 1 "
                    + "AND stock_actual - stock_reservado >= ?";

            try (PreparedStatement psDetalle =
                         cn.prepareStatement(sqlDetalle);
                 PreparedStatement psStock =
                         cn.prepareStatement(sqlStock)) {

                for (DetalleVenta d : detalles) {

                    psDetalle.setInt(1, idVenta);
                    psDetalle.setInt(2, d.getIdProducto());
                    psDetalle.setInt(3, d.getCantidad());
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario());
                    psDetalle.setBigDecimal(5, d.getSubtotal());
                    psDetalle.executeUpdate();

                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.setInt(3, d.getCantidad());

                    if (psStock.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo descontar el stock del producto: "
                                + d.getIdProducto());
                    }
                }
            }

            cn.commit();

            venta.setIdVenta(idVenta);
            venta.setTotal(total);

            return idVenta;

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

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();

        v.setIdVenta(rs.getInt("id_venta"));
        v.setFecha(rs.getTimestamp("fecha"));

        int idCliente = rs.getInt("id_cliente");
        v.setIdCliente(rs.wasNull() ? null : idCliente);

        v.setIdUsuario(rs.getInt("id_usuario"));

        int idPedido = rs.getInt("id_pedido");
        v.setIdPedido(rs.wasNull() ? null : idPedido);

        v.setTotal(rs.getBigDecimal("total"));
        v.setTipoComprobante(rs.getString("tipo_comprobante"));

        return v;
    }
}