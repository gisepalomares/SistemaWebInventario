package entidad;

/**
 * Captura de pantalla del pago (Yape/Plin) guardada en la tabla pago.
 * Solo se carga cuando el personal abre la imagen; no viaja dentro de Pedido.
 */
public class Comprobante {

    private final byte[] datos;
    private final String tipo;      // image/jpeg, image/png o image/webp

    public Comprobante(byte[] datos, String tipo) {
        this.datos = datos;
        this.tipo = tipo;
    }

    public byte[] getDatos() {
        return datos;
    }

    public String getTipo() {
        return tipo;
    }
}
