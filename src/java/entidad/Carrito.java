/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidad;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrito de compras del cliente. Se guarda en la sesión (atributo "carrito").
 * Agregar productos aquí NO descuenta ni reserva stock; eso ocurre al confirmar el pedido.
 */
public class Carrito implements Serializable {

    private final Map<Integer, ItemCarrito> items = new LinkedHashMap<>();

    public Collection<ItemCarrito> getItems() {
        return items.values();
    }

    public ItemCarrito obtener(int idProducto) {
        return items.get(idProducto);
    }

    public void poner(ItemCarrito item) {
        items.put(item.getIdProducto(), item);
    }

    public void quitar(int idProducto) {
        items.remove(idProducto);
    }

    public void vaciar() {
        items.clear();
    }

    public boolean isVacio() {
        return items.isEmpty();
    }

    /** Total de unidades (se muestra en el navbar). */
    public int getCantidadTotal() {
        int total = 0;
        for (ItemCarrito i : items.values()) {
            total += i.getCantidad();
        }
        return total;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito i : items.values()) {
            total = total.add(i.getSubtotal());
        }
        return total;
    }
}