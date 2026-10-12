package dao;

import entidad.Usuario;
import interfaces.IUsuarioDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.ConexionBD;
import util.Roles;

public class UsuarioDAO implements IUsuarioDAO {

    private static final String SELECT_BASE
            = "SELECT u.id_usuario, u.nombres, u.correo, u.contrasena, u.id_rol, "
            + "r.nombre_rol, u.estado "
            + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol ";

    @Override
    public Usuario buscarPorCorreo(String correo) throws SQLException {
        String sql = SELECT_BASE + "WHERE u.correo = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String sql = SELECT_BASE + "WHERE u.id_usuario = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean registrar(Usuario u) throws SQLException {
        // El id del rol se obtiene por su nombre, así no hay que memorizar números
        String sql = "INSERT INTO usuario (nombres, correo, contrasena, id_rol, estado) "
                + "SELECT ?, ?, ?, id_rol, ? FROM rol WHERE nombre_rol = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getContrasena());
            ps.setBoolean(4, u.isEstado());
            ps.setString(5, u.getRol());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Usuario> listarPersonal() throws SQLException {
        String sql = SELECT_BASE + "WHERE r.nombre_rol IN (?, ?) ORDER BY r.nombre_rol, u.nombres";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, Roles.ADMINISTRADOR);
            ps.setString(2, Roles.VENDEDOR);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = mapear(rs);
                    u.setContrasena(null);   // el hash no se envía a las pantallas
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(int idUsuario, String nombres, String rol) throws SQLException {
        String sql = "UPDATE usuario SET nombres = ?, "
                + "id_rol = (SELECT id_rol FROM rol WHERE nombre_rol = ?) "
                + "WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombres);
            ps.setString(2, rol);
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        String sql = "UPDATE usuario SET estado = ? WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cambiarClave(int idUsuario, String hash) throws SQLException {
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int contarAdministradoresActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "
                + "WHERE r.nombre_rol = ? AND u.estado = 1";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, Roles.ADMINISTRADOR);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombres(rs.getString("nombres"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasena(rs.getString("contrasena"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setRol(rs.getString("nombre_rol"));
        u.setEstado(rs.getBoolean("estado"));
        return u;
    }
}
