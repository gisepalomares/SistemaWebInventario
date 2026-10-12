/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entidad.Proveedor;
import interfaces.IProveedorDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.ConexionBD;

public class ProveedorDAO implements IProveedorDAO {

    private static final String SELECT_BASE
            = "SELECT id_proveedor, ruc, razon_social, nombre_comercial FROM proveedor ";

    @Override
    public List<Proveedor> listar() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY COALESCE(NULLIF(nombre_comercial, ''), razon_social)";
        List<Proveedor> lista = new ArrayList<>();
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
    public Proveedor buscarPorId(int idProveedor) throws SQLException {
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE id_proveedor = ?")) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setRuc(rs.getString("ruc"));
        p.setRazonSocial(rs.getString("razon_social"));
        p.setNombreComercial(rs.getString("nombre_comercial"));
        return p;
    }
}