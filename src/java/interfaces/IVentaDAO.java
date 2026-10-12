/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;
import entidad.Venta;
import entidad.DetalleVenta;
import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author Alex
 */
public interface IVentaDAO {
     List<Venta> listar() throws SQLException;

    Venta buscarPorId(int idVenta) throws SQLException;

    List<DetalleVenta> listarDetalles(int idVenta) throws SQLException;

    int registrar(Venta venta, List<DetalleVenta> detalles)
            throws SQLException;
}
