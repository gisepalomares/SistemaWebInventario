package entidad;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Compra de mercadería a un proveedor (entrada al almacén).
 *
 * @author Diego
 */
public class Compra {

    private int idCompra;
    private Timestamp fecha;
    private int idProveedor;
    private String proveedor;
    private BigDecimal total;
    private String estado;

    public Compra() {
    }

    public boolean isAnulada() {
        return "ANULADA".equals(estado);
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
