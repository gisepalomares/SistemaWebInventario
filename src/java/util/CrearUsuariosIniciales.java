/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import dao.UsuarioDAO;
import entidad.Usuario;
import interfaces.IUsuarioDAO;
import java.sql.SQLException;

/**
 * HERRAMIENTA DE DESARROLLO: crea el primer administrador y un vendedor de
 * prueba (con la contraseña ya convertida en hash).
 *
 * Se ejecuta UNA vez con clic derecho -> Run File. Si el usuario ya existe,
 * no hace nada. Cambie los correos/contraseñas por los suyos; más adelante la
 * gestión de usuarios se hará desde el sistema y esta clase se puede borrar.
 */
public class CrearUsuariosIniciales {

    public static void main(String[] args) {
        IUsuarioDAO dao = new UsuarioDAO();
        crear(dao, "Administrador General", "admin@electrospeed.com", "Admin123*", Roles.ADMINISTRADOR);
        crear(dao, "Vendedor de Prueba", "vendedor@electrospeed.com", "Vende123*", Roles.VENDEDOR);
    }

    private static void crear(IUsuarioDAO dao, String nombres, String correo, String clave, String rol) {
        try {
            if (dao.buscarPorCorreo(correo) != null) {
                System.out.println("Ya existe: " + correo);
                return;
            }
            Usuario u = new Usuario();
            u.setNombres(nombres);
            u.setCorreo(correo);
            u.setContrasena(SeguridadClave.hashear(clave));
            u.setRol(rol);
            u.setEstado(true);
            System.out.println(dao.registrar(u)
                    ? "Creado " + rol + ": " + correo
                    : "No se pudo crear " + correo + " (revise que exista el rol " + rol + ")");
        } catch (SQLException e) {
            System.out.println("Error con " + correo + ": " + e.getMessage());
        }
    }
}
