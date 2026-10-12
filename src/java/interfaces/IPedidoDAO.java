/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import entidad.Pedido;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import util.PedidoException;

public interface IPedidoDAO {

    // ------------------------------------------------------------------
    //  Zona pública (cliente)
    // ------------------------------------------------------------------
    /**
     * Registra el pedido y reserva el stock en UNA sola transacción.
     * Entrada: idCliente, metodoPago, estado y detalles (solo idProducto y cantidad).
     * Salida: el mismo objeto completado (id, código de recojo, precios, total y fechas).
     *
     * @throws PedidoException si algún producto ya no tiene stock suficiente (no se reserva nada)
     */
    void registrar(Pedido pedido) throws SQLException, PedidoException;

    /** Pedidos de una cuenta de cliente (los más recientes primero), cada uno con sus detalles. */
    List<Pedido> listarPorUsuario(int idUsuario) throws SQLException;

    // ------------------------------------------------------------------
    //  Zona interna (vendedor / administrador)
    // ------------------------------------------------------------------
    /**
     * Bandeja de pedidos con datos del cliente y sus detalles.
     *
     * @param estado estado a filtrar (null o vacío = todos)
     * @param texto  busca en código de recojo, nombre o DNI del cliente (null o vacío = sin filtro)
     */
    List<Pedido> listarGestion(String estado, String texto) throws SQLException;

    /** Cantidad de pedidos por estado (para los contadores de las pestañas). */
    Map<String, Integer> contarPorEstado() throws SQLException;

        /**
     * PENDIENTE_PAGO -> PAGADO. Registra el pago hecho en tienda.
     *
     * @param medio      "EFECTIVO" o "POS"
     * @param referencia N.° de operación del voucher (solo POS; null si es efectivo)
     */
    void registrarPagoEnTienda(int idPedido, String medio, String referencia) throws SQLException, PedidoException;
    
    /** PAGADO -> LISTO_RECOGER. */
    void marcarListo(int idPedido) throws SQLException, PedidoException;

    /**
     * LISTO_RECOGER -> ENTREGADO. Crea la venta y descuenta el stock real
     * (stock_actual y stock_reservado) en una sola transacción.
     *
     * @return el id de la venta creada
     */
    int entregar(int idPedido, int idUsuario, String codigoIngresado) throws SQLException, PedidoException;

    /** Pendiente, Pagado o Listo -> CANCELADO. Libera el stock reservado. */
    void cancelar(int idPedido) throws SQLException, PedidoException;

    /**
     * Vence los pedidos pendientes cuyo plazo terminó y libera su stock reservado.
     *
     * @return cuántos pedidos se vencieron
     */
    int vencerPedidos() throws SQLException;
}
