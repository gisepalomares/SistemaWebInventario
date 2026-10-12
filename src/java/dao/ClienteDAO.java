package dao;

import entidad.Usuario;
import interfaces.IClienteDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import util.ConexionBD;
import util.Roles;

public class ClienteDAO implements IClienteDAO {

    @Override
    public boolean existeDocumento(String documento) throws SQLException {
        String sql = "SELECT 1 FROM cliente WHERE documento = ?";
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Integer buscarIdPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT id_cliente FROM cliente WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_cliente");
                }
                return null;
            }
        }
    }

    @Override
    public boolean registrarConCuenta(Usuario u, String documento, String telefono) throws SQLException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setAutoCommit(false);
            try {
                // 1) ¿el DNI ya existe? (cliente atendido antes en tienda)
                Integer idClienteExistente = null;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT id_cliente, id_usuario FROM cliente WHERE documento = ? FOR UPDATE")) {
                    ps.setString(1, documento);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            rs.getInt("id_usuario");
                            if (!rs.wasNull()) {
                                cn.rollback();
                                return false;       // ya tiene cuenta
                            }
                            idClienteExistente = rs.getInt("id_cliente");
                        }
                    }
                }

                // 2) crear el usuario con rol CLIENTE (el rol siempre lo fija el sistema)
                int idUsuario;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO usuario (nombres, correo, contrasena, id_rol, estado) "
                        + "VALUES (?, ?, ?, (SELECT id_rol FROM rol WHERE nombre_rol = ?), 1)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, u.getNombres());
                    ps.setString(2, u.getCorreo());
                    ps.setString(3, u.getContrasena());
                    ps.setString(4, Roles.CLIENTE);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se obtuvo el id del usuario creado");
                        }
                        idUsuario = keys.getInt(1);
                    }
                }

                // 3) crear la ficha de cliente, o vincular la que ya existía
                if (idClienteExistente == null) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO cliente (nombres, documento, telefono, id_usuario) VALUES (?, ?, ?, ?)")) {
                        ps.setString(1, u.getNombres());
                        ps.setString(2, documento);
                        ps.setString(3, telefono);
                        ps.setInt(4, idUsuario);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE cliente SET id_usuario = ?, "
                            + "telefono = COALESCE(?, telefono) WHERE id_cliente = ?")) {
                        ps.setInt(1, idUsuario);
                        ps.setString(2, telefono);
                        ps.setInt(3, idClienteExistente);
                        ps.executeUpdate();
                    }
                }

                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }
}
