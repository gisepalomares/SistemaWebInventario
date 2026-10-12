/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import entidad.Proveedor;
import interfaces.IProveedorDAO;
import util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Alex
 */
public class ProveedorDAO implements IProveedorDAO{
    private static final String COLUMNAS =
            "id_proveedor, ruc, razon_social, nombre_comercial, "
            + "direccion, persona_contacto, telefono, correo";

    @Override
    public List<Proveedor> listar() throws SQLException {

        List<Proveedor> lista = new ArrayList<>();

        String sql = "SELECT " + COLUMNAS
                + " FROM proveedor ORDER BY id_proveedor";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    @Override
    public Proveedor buscarPorId(int idProveedor)
            throws SQLException {

        String sql = "SELECT " + COLUMNAS
                + " FROM proveedor WHERE id_proveedor = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Proveedor> buscar(String texto)
            throws SQLException {

        List<Proveedor> lista = new ArrayList<>();

        String sql = "SELECT " + COLUMNAS
                + " FROM proveedor "
                + "WHERE ruc LIKE ? "
                + "OR razon_social LIKE ? "
                + "ORDER BY razon_social";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            String filtro = "%" + texto + "%";

            ps.setString(1, filtro);
            ps.setString(2, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    @Override
    public boolean registrar(Proveedor p)
            throws SQLException {

        String sql = "INSERT INTO proveedor "
                + "(ruc, razon_social, nombre_comercial, "
                + "direccion, persona_contacto, telefono, correo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            asignarParametros(ps, p);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizar(Proveedor p)
            throws SQLException {

        String sql = "UPDATE proveedor SET "
                + "ruc = ?, razon_social = ?, "
                + "nombre_comercial = ?, direccion = ?, "
                + "persona_contacto = ?, telefono = ?, "
                + "correo = ? WHERE id_proveedor = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            asignarParametros(ps, p);
            ps.setInt(8, p.getIdProveedor());

            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean eliminar(int idProveedor) throws SQLException {

        String sql = "DELETE FROM proveedor "
                   + "WHERE id_proveedor = ?";

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);

            return ps.executeUpdate() > 0;
        }
    }


    private void asignarParametros(
            PreparedStatement ps, Proveedor p)
            throws SQLException {

        ps.setString(1, p.getRuc());
        ps.setString(2, p.getRazonSocial());
        ps.setString(3, p.getNombreComercial());
        ps.setString(4, p.getDireccion());
        ps.setString(5, p.getPersonaContacto());
        ps.setString(6, p.getTelefono());
        ps.setString(7, p.getCorreo());
    }

    private Proveedor mapear(ResultSet rs)
            throws SQLException {

        Proveedor p = new Proveedor();

        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setRuc(rs.getString("ruc"));
        p.setRazonSocial(rs.getString("razon_social"));
        p.setNombreComercial(
                rs.getString("nombre_comercial"));
        p.setDireccion(rs.getString("direccion"));
        p.setPersonaContacto(
                rs.getString("persona_contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));

        return p;
    }
}
