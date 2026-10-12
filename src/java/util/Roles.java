/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author USER
 */
public final class Roles {
 public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String VENDEDOR = "VENDEDOR";
    public static final String CLIENTE = "CLIENTE";

    private Roles() {
    }

    /** El personal de la tienda (puede entrar al panel interno). */
    public static boolean esPersonal(String rol) {
        return ADMINISTRADOR.equals(rol) || VENDEDOR.equals(rol);
    }
}