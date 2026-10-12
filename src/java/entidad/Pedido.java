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
    public static final String PAGADO = "PAGADO";
    public static final String LISTO_RECOGER = "LISTO_RECOGER";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String VENCIDO = "VENCIDO";
    public static final String CANCELADO = "CANCELADO";

    public static final String METODO_EN_LINEA = "EN_LINEA_SIMULADO";
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
        if (METODO_EN_TIENDA.equals(metodoPago)) {
            return "Pago en tienda";
        }
        return metodoPago;
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
