package interfaces;

import entidad.Usuario;
import java.sql.SQLException;
import java.util.List;

public interface IUsuarioDAO {

    /** Devuelve el usuario con su rol, o null si el correo no existe. */
    Usuario buscarPorCorreo(String correo) throws SQLException;

    /** Devuelve el usuario con su rol, o null si el id no existe. */
    Usuario buscarPorId(int idUsuario) throws SQLException;

    /**
     * Registra un usuario. Usa usuario.getRol() (nombre del rol) para buscar el
     * id del rol; la contraseña debe venir ya convertida en hash.
     *
     * @return true si se insertó
     */
    boolean registrar(Usuario usuario) throws SQLException;

    /** Lista al personal de la tienda (ADMINISTRADOR y VENDEDOR). */
    List<Usuario> listarPersonal() throws SQLException;

    /** Cambia nombres y rol de un usuario del personal. */
    boolean actualizar(int idUsuario, String nombres, String rol) throws SQLException;

    /** Activa o desactiva una cuenta. */
    boolean cambiarEstado(int idUsuario, boolean activo) throws SQLException;

    /** Guarda una nueva contraseña (ya convertida en hash). */
    boolean cambiarClave(int idUsuario, String hash) throws SQLException;

    /** Cantidad de administradores activos (para no dejar el sistema sin ninguno). */
    int contarAdministradoresActivos() throws SQLException;
}
