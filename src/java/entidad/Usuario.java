/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidad;

/**
 *
 * @author USER
 */
import util.Roles;

/**
 * Usuario del sistema (personal o cliente). Corresponde a la tabla usuario,
 * junto con el nombre de su rol (tabla rol).
 */
public class Usuario {

    private int idUsuario;
    private String nombres;
    private String correo;
    private String contrasena;   // hash; se borra (null) antes de guardar el usuario en la sesión
    private int idRol;
    private String rol;          // ADMINISTRADOR, VENDEDOR o CLIENTE
    private boolean estado;

    public Usuario() {
    }

    /** Administrador o vendedor: puede entrar al panel interno. */
    public boolean isPersonal() {
        return Roles.esPersonal(rol);
    }

    public boolean isAdministrador() {
        return Roles.ADMINISTRADOR.equals(rol);
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
}