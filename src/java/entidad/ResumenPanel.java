package entidad;

import java.math.BigDecimal;

/**
 * Números que muestra el dashboard interno (un solo objeto para no hacer una consulta por tarjeta).
 */
public class ResumenPanel {

    private int ventasHoyCantidad;
    private BigDecimal ventasHoyTotal = BigDecimal.ZERO;
    private int ventasMesCantidad;
    private BigDecimal ventasMesTotal = BigDecimal.ZERO;

    private int stockBajo;            // mismo criterio que el módulo Productos: con stock, pero <= mínimo
    private int agotados;             // disponible (stock - reservado) <= 0

    private int pedidosPorVerificar;  // PAGO_EN_REVISION (captura Yape/Plin por revisar)
    private int pedidosPorCobrar;     // PENDIENTE_PAGO, dentro de su plazo
    private int pedidosPorPreparar;   // PAGADO
    private int pedidosPorEntregar;   // LISTO_RECOGER

    public int getVentasHoyCantidad() { return ventasHoyCantidad; }
    public void setVentasHoyCantidad(int v) { this.ventasHoyCantidad = v; }

    public BigDecimal getVentasHoyTotal() { return ventasHoyTotal; }
    public void setVentasHoyTotal(BigDecimal v) { this.ventasHoyTotal = v; }

    public int getVentasMesCantidad() { return ventasMesCantidad; }
    public void setVentasMesCantidad(int v) { this.ventasMesCantidad = v; }

    public BigDecimal getVentasMesTotal() { return ventasMesTotal; }
    public void setVentasMesTotal(BigDecimal v) { this.ventasMesTotal = v; }

    public int getStockBajo() { return stockBajo; }
    public void setStockBajo(int v) { this.stockBajo = v; }

    public int getAgotados() { return agotados; }
    public void setAgotados(int v) { this.agotados = v; }

    public int getPedidosPorVerificar() { return pedidosPorVerificar; }
    public void setPedidosPorVerificar(int v) { this.pedidosPorVerificar = v; }

    public int getPedidosPorCobrar() { return pedidosPorCobrar; }
    public void setPedidosPorCobrar(int v) { this.pedidosPorCobrar = v; }

    public int getPedidosPorPreparar() { return pedidosPorPreparar; }
    public void setPedidosPorPreparar(int v) { this.pedidosPorPreparar = v; }

    public int getPedidosPorEntregar() { return pedidosPorEntregar; }
    public void setPedidosPorEntregar(int v) { this.pedidosPorEntregar = v; }
}
