/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import dao.UsuarioDAO;
import entidad.Usuario;

public class ProbarConexion {
    public static void main(String[] args) {
        try {
            Usuario u = new UsuarioDAO().buscarPorCorreo("admin@electrospeed.com");
            if (u == null) {
                System.out.println("NO ENCONTRADO: el correo no existe en la tabla");
            } else {
                System.out.println("Encontrado: " + u.getNombres() + " rol=" + u.getRol()
                        + " estado=" + u.isEstado());
                System.out.println("Clave correcta: "
                        + SeguridadClave.verificar("Admin123*", u.getContrasena()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}