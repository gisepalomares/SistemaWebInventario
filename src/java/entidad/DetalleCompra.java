package entidad;

import java.math.BigDecimal;

/**
 * Línea de una compra: producto, cantidad y precio de compra.
 *
 * @author Diego
 */
public class DetalleCompra {

    private int idDetalleCompra;
    private int idCompra;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleCompra() {
    }

    /** La tabla no guarda el subtotal: se calcula. */
    public BigDecimal getSubtotal() {
        return precioUnitario == null ? BigDecimal.ZERO
                : precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
