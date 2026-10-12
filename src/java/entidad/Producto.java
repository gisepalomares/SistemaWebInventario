/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidad;

import java.math.BigDecimal;

/**
 * Producto del inventario. Se llena desde la vista v_producto_disponible, por
 * eso incluye el nombre de la categoría y el stock disponible real.
 */
public class Producto {

    private int idProducto;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private BigDecimal precioVenta;
    private int stockActual;
    private int stockReservado;
    private int stockMinimo;
    private int idCategoria;
    private String categoria;
    private Integer idProveedor;
    private boolean estado;
    private String proveedor;

    public Producto() {
    }

    /**
     * Stock que realmente se puede vender (actual - reservado por pedidos).
     */
    public int getStockDisponible() {
        return stockActual - stockReservado;
    }

    public boolean isAgotado() {
        return getStockDisponible() <= 0;
    }

    /**
     * Hay stock, pero igual o por debajo del mínimo configurado.
     */
    public boolean isStockBajo() {
        return !isAgotado() && getStockDisponible() <= stockMinimo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoSku() {
        return codigoSku;
    }

    public void setCodigoSku(String codigoSku) {
        this.codigoSku = codigoSku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockReservado() {
        return stockReservado;
    }

    public void setStockReservado(int stockReservado) {
        this.stockReservado = stockReservado;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
}
