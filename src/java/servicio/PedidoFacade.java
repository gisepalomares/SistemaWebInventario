/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicio;

import dao.ClienteDAO;
import dao.PedidoDAO;
import entidad.Carrito;
import entidad.Comprobante;
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

    /** Tamaño máximo de la captura de pago (debe coincidir con @MultipartConfig del servlet). */
    public static final int MAX_BYTES_COMPROBANTE = 5 * 1024 * 1024;

    /**
     * Pedido con PAGO EN TIENDA: valida, reserva el stock por 48 horas y lo registra.
     * El pago en línea no pasa por aquí: ver confirmarPedidoQR().
     *
     * @param metodoPago debe ser EN_TIENDA
     */
    public Pedido confirmarPedido(Usuario usuario, Carrito carrito, String metodoPago)
            throws SQLException, PedidoException {

        // Importante: cualquier otro valor (por ejemplo, el antiguo "en línea simulado" que alguien
        // podría forzar editando el formulario) se rechaza: no se crean pedidos "pagados" sin pagar.
        if (!Pedido.METODO_EN_TIENDA.equals(metodoPago)) {
            throw new PedidoException("Elija una forma de pago.");
        }
        Pedido pedido = armarPedido(usuario, carrito, Pedido.METODO_EN_TIENDA, Pedido.PENDIENTE_PAGO);
        pedidoDAO.registrar(pedido);
        return pedido;
    }

    /**
     * Pedido con PAGO EN LÍNEA (Yape o Plin): reserva el stock y guarda la captura del pago.
     * El pedido queda en PAGO_EN_REVISION hasta que el vendedor verifique que el dinero llegó.
     *
     * @param medio  YAPE o PLIN
     * @param imagen bytes de la captura de pantalla (JPG, PNG o WEBP, máx. 5 MB)
     */
    public Pedido confirmarPedidoQR(Usuario usuario, Carrito carrito, String medio, byte[] imagen)
            throws SQLException, PedidoException {

        if (!"YAPE".equals(medio) && !"PLIN".equals(medio)) {
            throw new PedidoException("Elija si pagó con Yape o con Plin.");
        }
        if (imagen == null || imagen.length == 0) {
            throw new PedidoException("Suba la captura de pantalla de su pago para enviar el pedido.");
        }
        if (imagen.length > MAX_BYTES_COMPROBANTE) {
            throw new PedidoException("La imagen es muy pesada. El máximo es 5 MB.");
        }
        // El tipo se deduce de los bytes de la imagen, nunca del nombre ni del tipo que declara el navegador
        String tipo = detectarTipoImagen(imagen);
        if (tipo == null) {
            throw new PedidoException("El archivo no es una imagen válida. Suba una captura en JPG, PNG o WEBP.");
        }

        Pedido pedido = armarPedido(usuario, carrito, Pedido.METODO_EN_LINEA_QR, Pedido.PAGO_EN_REVISION);
        pedido.setMedioQR(medio);
        pedidoDAO.registrarConComprobante(pedido, medio, imagen, tipo);
        return pedido;
    }

    /** Valida el carrito y el cliente, y arma el pedido (solo viajan id y cantidad: el precio lo lee el DAO). */
    private Pedido armarPedido(Usuario usuario, Carrito carrito, String metodoPago, String estado)
            throws SQLException, PedidoException {

        if (carrito == null || carrito.isVacio()) {
            throw new PedidoException("Su carrito está vacío.");
        }
        Integer idCliente = clienteDAO.buscarIdPorUsuario(usuario.getIdUsuario());
        if (idCliente == null) {
            throw new PedidoException("Su cuenta no tiene una ficha de cliente asociada.");
        }

        Pedido pedido = new Pedido();
        pedido.setIdCliente(idCliente);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(estado);

        for (ItemCarrito item : carrito.getItems()) {
            DetallePedido d = new DetallePedido();
            d.setIdProducto(item.getIdProducto());
            d.setCantidad(item.getCantidad());
            pedido.getDetalles().add(d);
        }
        return pedido;
    }

    /** "image/jpeg", "image/png", "image/webp" según los primeros bytes; null si no es una de esas imágenes. */
    static String detectarTipoImagen(byte[] b) {
        if (b == null || b.length < 12) {
            return null;
        }
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
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

    /** El vendedor verificó que el dinero llegó a la cuenta: PAGO_EN_REVISION -> PAGADO. */
    public void confirmarPagoQR(int idPedido) throws SQLException, PedidoException {
        pedidoDAO.confirmarPagoQR(idPedido);
    }

    /** El pago no llegó: PAGO_EN_REVISION -> CANCELADO y se libera el stock. */
    public void rechazarPagoQR(int idPedido) throws SQLException, PedidoException {
        pedidoDAO.rechazarPagoQR(idPedido);
    }

    public Comprobante obtenerComprobante(int idPago) throws SQLException {
        return pedidoDAO.obtenerComprobante(idPago);
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
