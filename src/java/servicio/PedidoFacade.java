/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicio;

import dao.ClienteDAO;
import dao.PedidoDAO;
import entidad.Carrito;
import entidad.DetallePedido;
import entidad.ItemCarrito;
import entidad.Pedido;
import entidad.Usuario;
import interfaces.IClienteDAO;
import interfaces.IPedidoDAO;
import java.sql.SQLException;
import util.PedidoException;
import java.util.List;
import java.util.Map;

/**
 * Façade del pedido en línea: agrupa los pasos de "confirmar pedido" para que
 * el servlet no conozca los DAO ni las reglas de negocio.
 */
public class PedidoFacade {

    private final IPedidoDAO pedidoDAO = new PedidoDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();

    /**
     * Confirma el pedido del carrito: valida, reserva el stock y lo registra.
     *
     * @param metodoPago EN_TIENDA o EN_LINEA_SIMULADO
     * @param simularRechazo solo aplica al pago en línea: simula que el pago
     * fue rechazado
     */
    public Pedido confirmarPedido(Usuario usuario, Carrito carrito, String metodoPago, boolean simularRechazo)
            throws SQLException, PedidoException {

        if (carrito == null || carrito.isVacio()) {
            throw new PedidoException("Su carrito está vacío.");
        }
        boolean enLinea = Pedido.METODO_EN_LINEA.equals(metodoPago);
        boolean enTienda = Pedido.METODO_EN_TIENDA.equals(metodoPago);
        if (!enLinea && !enTienda) {
            throw new PedidoException("Elija una forma de pago.");
        }
        if (enLinea && simularRechazo) {
            throw new PedidoException("El pago en línea fue rechazado (simulación). "
                    + "No se reservó stock; puede intentarlo de nuevo o elegir pagar en tienda.");
        }

        Integer idCliente = clienteDAO.buscarIdPorUsuario(usuario.getIdUsuario());
        if (idCliente == null) {
            throw new PedidoException("Su cuenta no tiene una ficha de cliente asociada.");
        }

        Pedido pedido = new Pedido();
        pedido.setIdCliente(idCliente);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(enLinea ? Pedido.PAGADO : Pedido.PENDIENTE_PAGO);

        // Solo viajan id y cantidad: el precio y el nombre los lee el DAO desde la BD
        for (ItemCarrito item : carrito.getItems()) {
            DetallePedido d = new DetallePedido();
            d.setIdProducto(item.getIdProducto());
            d.setCantidad(item.getCantidad());
            pedido.getDetalles().add(d);
        }

        pedidoDAO.registrar(pedido);
        return pedido;
    }
    // ------------------------------------------------------------------
    //  Zona interna (vendedor / administrador)
    // ------------------------------------------------------------------

    public List<Pedido> listarGestion(String estado, String texto) throws SQLException {
        return pedidoDAO.listarGestion(estado, texto);
    }

    public Map<String, Integer> contarPorEstado() throws SQLException {
        return pedidoDAO.contarPorEstado();
    }

        public void registrarPagoEnTienda(int idPedido, String medio, String referencia)
            throws SQLException, PedidoException {

        String ref = referencia == null ? "" : referencia.trim();

        if ("POS".equals(medio)) {
            // Con tarjeta sí hay voucher y su N.° de operación es obligatorio
            if (!ref.matches("[A-Za-z0-9\\-]{3,60}")) {
                throw new PedidoException("Ingrese el N.° de operación del voucher del POS "
                        + "(de 3 a 60 caracteres: letras, números o guiones).");
            }
        } else if ("EFECTIVO".equals(medio)) {
            ref = null;   // en efectivo no hay voucher
        } else {
            throw new PedidoException("Elija el medio de pago: efectivo o tarjeta (POS).");
        }
        pedidoDAO.registrarPagoEnTienda(idPedido, medio, ref);
    }

    public void marcarListo(int idPedido) throws SQLException, PedidoException {
        pedidoDAO.marcarListo(idPedido);
    }

    /**
     * @return el id de la venta generada
     */
    public int entregar(int idPedido, Usuario vendedor, String codigoRecojo) throws SQLException, PedidoException {
        if (codigoRecojo == null || codigoRecojo.trim().isEmpty()) {
            throw new PedidoException("Ingrese el código de recojo que presenta el cliente.");
        }
        return pedidoDAO.entregar(idPedido, vendedor.getIdUsuario(), codigoRecojo);
    }

    public void cancelar(int idPedido) throws SQLException, PedidoException {
        pedidoDAO.cancelar(idPedido);
    }

    /**
     * Vence los pedidos pendientes cuyo plazo terminó. @return cuántos venció
     */
    public int vencerPedidos() throws SQLException {
        return pedidoDAO.vencerPedidos();
    }
}
