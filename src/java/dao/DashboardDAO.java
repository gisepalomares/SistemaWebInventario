package dao;

import entidad.ResumenPanel;
import interfaces.IDashboardDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.ConexionBD;

public class DashboardDAO implements IDashboardDAO {

    @Override
    public ResumenPanel obtenerResumen() throws SQLException {
        ResumenPanel r = new ResumenPanel();
        try (Connection cn = ConexionBD.conectar()) {

            // Ventas de hoy (con el reloj de MySQL, el mismo que guarda venta.fecha)
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM venta "
                    + "WHERE fecha >= CURDATE() AND fecha < CURDATE() + INTERVAL 1 DAY");
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setVentasHoyCantidad(rs.getInt(1));
                    r.setVentasHoyTotal(rs.getBigDecimal(2));
                }
            }

            // Ventas del mes en curso (desde el día 1 hasta hoy)
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM venta "
                    + "WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL DAYOFMONTH(CURDATE()) - 1 DAY) "
                    + "AND fecha < CURDATE() + INTERVAL 1 DAY");
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setVentasMesCantidad(rs.getInt(1));
                    r.setVentasMesTotal(rs.getBigDecimal(2));
                }
            }

            // Alertas de stock (solo productos activos). Mismo criterio que Producto.isAgotado()/isStockBajo()
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT "
                    + "COALESCE(SUM(stock_actual - stock_reservado <= 0), 0), "
                    + "COALESCE(SUM(stock_actual - stock_reservado > 0 "
                    + "AND stock_actual - stock_reservado <= stock_minimo), 0) "
                    + "FROM producto WHERE estado = 1");
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setAgotados(rs.getInt(1));
                    r.setStockBajo(rs.getInt(2));
                }
            }

            // Pedidos en línea que esperan una acción del personal
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT "
                    + "COALESCE(SUM(estado = 'PAGO_EN_REVISION'), 0), "
                    + "COALESCE(SUM(estado = 'PENDIENTE_PAGO' "
                    + "AND (fecha_vencimiento IS NULL OR fecha_vencimiento >= NOW())), 0), "
                    + "COALESCE(SUM(estado = 'PAGADO'), 0), "
                    + "COALESCE(SUM(estado = 'LISTO_RECOGER'), 0) "
                    + "FROM pedido");
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setPedidosPorVerificar(rs.getInt(1));
                    r.setPedidosPorCobrar(rs.getInt(2));
                    r.setPedidosPorPreparar(rs.getInt(3));
                    r.setPedidosPorEntregar(rs.getInt(4));
                }
            }
        }
        return r;
    }
}
