/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entidad.Categoria;
import interfaces.ICategoriaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.ConexionBD;

public class CategoriaDAO implements ICategoriaDAO {

    private static final String SELECT_BASE
            = "SELECT id_categoria, nombre, descripcion FROM categoria ";

    @Override
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Categoria> listarConTotales() throws SQLException {
        // Se cuentan TODOS los productos (activos e inactivos): una categoría solo se
        // puede eliminar si ningún producto, ni siquiera inactivo, depende de ella.
        String sql = "SELECT c.id_categoria, c.nombre, c.descripcion, COUNT(p.id_producto) AS total "
                + "FROM categoria c LEFT JOIN producto p ON p.id_categoria = c.id_categoria "
                + "GROUP BY c.id_categoria, c.nombre, c.descripcion "
                + "ORDER BY c.nombre";
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria c = mapear(rs);
                c.setTotalProductos(rs.getInt("total"));
                lista.add(c);
            }
        }
        return lista;
    }

    @Override
    public Categoria buscarPorId(int idCategoria) throws SQLException {
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE id_categoria = ?")) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Categoria buscarPorNombre(String nombre) throws SQLException {
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean registrar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, vacioANulo(c.getDescripcion()));
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizar(Categoria c) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria = ?";
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, vacioANulo(c.getDescripcion()));
            ps.setInt(3, c.getIdCategoria());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int idCategoria) throws SQLException {
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM categoria WHERE id_categoria = ?")) {
            ps.setInt(1, idCategoria);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int contarProductos(int idCategoria) throws SQLException {
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM producto WHERE id_categoria = ?")) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        return c;
    }

    private String vacioANulo(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? null : texto.trim();
    }
}