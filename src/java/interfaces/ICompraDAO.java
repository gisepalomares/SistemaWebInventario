package interfaces;

import entidad.Compra;
import entidad.DetalleCompra;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Diego
 */
public interface ICompraDAO {

    List<Compra> listar() throws SQLException;

    Compra buscarPorId(int idCompra) throws SQLException;

    List<DetalleCompra> listarDetalles(int idCompra) throws SQLException;

    /** Registra la compra y suma las cantidades al stock. Devuelve el ID generado. */
    int registrar(Compra compra, List<DetalleCompra> detalles) throws SQLException;

    /** Marca la compra como ANULADA y resta del stock lo que había ingresado. */
    void anular(int idCompra) throws SQLException;
}
