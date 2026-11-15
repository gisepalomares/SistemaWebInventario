/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidad;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido en línea. Los valores de estado y método coinciden con los ENUM de la
 * tabla pedido. Las fechas se guardan ya formateadas (texto) para evitar
 * desfases de zona horaria.
 */
public class Pedido implements Serializable {

    public static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    /** El cliente ya subió la captura de Yape/Plin; el vendedor debe verificar que el dinero llegó. */
    public static final String PAGO_EN_REVISION = "PAGO_EN_REVISION";
    public static final String PAGADO = "PAGADO";
    public static final String LISTO_RECOGER = "LISTO_RECOGER";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String VENCIDO = "VENCIDO";
    public static final String CANCELADO = "CANCELADO";

    /** Pago en línea simulado: solo existe en pedidos antiguos, ya no se puede elegir. */
    public static final String METODO_EN_LINEA = "EN_LINEA_SIMULADO";
    /** Pago en línea con Yape o Plin (el cliente sube la captura del pago). */
    public static final String METODO_EN_LINEA_QR = "EN_LINEA_QR";
    public static final String METODO_EN_TIENDA = "EN_TIENDA";

    private int idPedido;
    private String estado;
    private String codigoRecojo;
    private String metodoPago;
    private BigDecimal total = BigDecimal.ZERO;
    private int idCliente;
    private String fechaPedidoTexto;
    private String fechaVencimientoTexto;     // null si no aplica (pago en línea)
    private String clienteNombre;
    private String clienteDocumento;
    private String clienteTelefono;
    private int idPagoComprobante;            // 0 si el pedido no tiene captura de pago
    private String medioQR;                   // YAPE o PLIN (solo pedidos EN_LINEA_QR)
    private final List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public String getEstadoTexto() {
        if (estado == null) {
            return "";
        }
        switch (estado) {
            case PENDIENTE_PAGO:
                return "Pendiente de pago";
            case PAGO_EN_REVISION:
                return "Pago en revisión";
            case PAGADO:
                return "Pagado";
            case LISTO_RECOGER:
                return "Listo para recoger";
            case ENTREGADO:
                return "Entregado";
            case VENCIDO:
                return "Vencido";
            case CANCELADO:
                return "Cancelado";
            default:
                return estado;
        }
    }

    public String getMetodoPagoTexto() {
        if (METODO_EN_LINEA.equals(metodoPago)) {
            return "Pago en línea (simulado)";
        }
        if (METODO_EN_LINEA_QR.equals(metodoPago)) {
            String medio = getMedioQRTexto();
            return medio.isEmpty() ? "Pago en línea (Yape/Plin)" : "Pago en línea con " + medio;
        }
        if (METODO_EN_TIENDA.equals(metodoPago)) {
            return "Pago en tienda";
        }
        return metodoPago;
    }

    /** "Yape", "Plin" o "" si no aplica. */
    public String getMedioQRTexto() {
        if ("YAPE".equals(medioQR)) {
            return "Yape";
        }
        if ("PLIN".equals(medioQR)) {
            return "Plin";
        }
        return "";
    }

    public int getIdPagoComprobante() {
        return idPagoComprobante;
    }

    public void setIdPagoComprobante(int idPagoComprobante) {
        this.idPagoComprobante = idPagoComprobante;
    }

    public String getMedioQR() {
        return medioQR;
    }

    public void setMedioQR(String medioQR) {
        this.medioQR = medioQR;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigoRecojo() {
        return codigoRecojo;
    }

    public void setCodigoRecojo(String codigoRecojo) {
        this.codigoRecojo = codigoRecojo;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getFechaPedidoTexto() {
        return fechaPedidoTexto;
    }

    public void setFechaPedidoTexto(String fechaPedidoTexto) {
        this.fechaPedidoTexto = fechaPedidoTexto;
    }

    public String getFechaVencimientoTexto() {
        return fechaVencimientoTexto;
    }

    public void setFechaVencimientoTexto(String fechaVencimientoTexto) {
        this.fechaVencimientoTexto = fechaVencimientoTexto;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteDocumento() {
        return clienteDocumento;
    }

    public void setClienteDocumento(String clienteDocumento) {
        this.clienteDocumento = clienteDocumento;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }
}
