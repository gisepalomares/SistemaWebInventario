package dao;

import entidad.Producto;
import interfaces.IProductoDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import util.ConexionBD;

public class ProductoDAO implements IProductoDAO {

    // ---- Catálogo público: solo activos, directo desde la vista ----
    private static final String SELECT_BASE
            = "SELECT id_producto, codigo_sku, nombre, descripcion, precio_venta, "
            + "stock_actual, stock_reservado, stock_minimo, id_categoria, categoria, "
            + "id_proveedor, estado "
            + "FROM v_producto_disponible WHERE estado = 1 ";

    // ---- Gestión interna: con el nombre del proveedor (las columnas llevan prefijo v. para evitar ambigüedad) ----
    private static final String SELECT_GESTION
            = "SELECT v.id_producto, v.codigo_sku, v.nombre, v.descripcion, v.precio_venta, "
            + "v.stock_actual, v.stock_reservado, v.stock_minimo, v.id_categoria, v.categoria, "
            + "v.id_proveedor, v.estado, "
            + "COALESCE(NULLIF(pr.nombre_comercial, ''), pr.razon_social) AS proveedor "
            + "FROM v_producto_disponible v "
            + "LEFT JOIN proveedor pr ON pr.id_proveedor = v.id_proveedor ";

    @Override
    public List<Producto> listarCatalogo(String texto, Integer idCategoria, boolean soloConStock)
            throws SQLException {

        // El SQL se arma con condiciones fijas; los valores SIEMPRE van como parámetros (?)
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append("AND (nombre LIKE ? OR codigo_sku LIKE ?) ");
            String patron = "%" + texto.trim() + "%";
            params.add(patron);
            params.add(patron);
        }
        if (idCategoria != null) {
            sql.append("AND id_categoria = ? ");
            params.add(idCategoria);
        }
        if (soloConStock) {
            sql.append("AND stock_disponible > 0 ");
        }
        sql.append("ORDER BY categoria, nombre");

        return consultar(sql.toString(), params, false);
    }

    @Override
    public Producto buscarPorId(int idProducto) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(idProducto);
        List<Producto> r = consultar(SELECT_BASE + "AND id_producto = ?", params, false);
        return r.isEmpty() ? null : r.get(0);
    }

    // ------------------------------------------------------------------
    //  Zona interna
    // ------------------------------------------------------------------
    @Override
    public List<Producto> listarGestion(String texto, Integer idCategoria, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_GESTION).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if ("activos".equals(estado)) {
            sql.append("AND v.estado = 1 ");
        } else if ("inactivos".equals(estado)) {
            sql.append("AND v.estado = 0 ");
        }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append("AND (v.nombre LIKE ? OR v.codigo_sku LIKE ?) ");
            String patron = "%" + texto.trim() + "%";
            params.add(patron);
            params.add(patron);
        }
        if (idCategoria != null) {
            sql.append("AND v.id_categoria = ? ");
            params.add(idCategoria);
        }
        sql.append("ORDER BY v.categoria, v.nombre");

        return consultar(sql.toString(), params, true);
    }

    @Override
    public Producto obtenerParaGestion(int idProducto) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(idProducto);
        List<Producto> r = consultar(SELECT_GESTION + "WHERE v.id_producto = ?", params, true);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public Producto buscarPorSku(String codigoSku) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(codigoSku);
        List<Producto> r = consultar(SELECT_GESTION + "WHERE v.codigo_sku = ?", params, true);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public boolean registrar(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo_sku, nombre, descripcion, precio_venta, "
                + "stock_actual, stock_reservado, stock_minimo, id_categoria, id_proveedor, estado) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, 1)";
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigoSku());
            ps.setString(2, p.getNombre());
            asignarTextoOpcional(ps, 3, p.getDescripcion());
            ps.setBigDecimal(4, p.getPrecioVenta());
            ps.setInt(5, p.getStockActual());
            ps.setInt(6, p.getStockMinimo());
            ps.setInt(7, p.getIdCategoria());
            asignarEnteroOpcional(ps, 8, p.getIdProveedor());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizar(Producto p) throws SQLException {
        // Nota: el stock NO se actualiza aquí a propósito.
        String sql = "UPDATE producto SET codigo_sku = ?, nombre = ?, descripcion = ?, "
                + "precio_venta = ?, stock_minimo = ?, id_categoria = ?, id_proveedor = ? "
                + "WHERE id_producto = ?";
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigoSku());
            ps.setString(2, p.getNombre());
            asignarTextoOpcional(ps, 3, p.getDescripcion());
            ps.setBigDecimal(4, p.getPrecioVenta());
            ps.setInt(5, p.getStockMinimo());
            ps.setInt(6, p.getIdCategoria());
            asignarEnteroOpcional(ps, 7, p.getIdProveedor());
            ps.setInt(8, p.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cambiarEstado(int idProducto, boolean activo) throws SQLException {
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement("UPDATE producto SET estado = ? WHERE id_producto = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    //  Auxiliares
    // ------------------------------------------------------------------
    private List<Producto> consultar(String sql, List<Object> params, boolean conProveedor) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = mapear(rs);

                    if (conProveedor) {
                        p.setIdProveedor(rs.getInt("id_proveedor"));
                        p.setProveedor(rs.getString("proveedor"));
                    }

                    lista.add(p);
                }
            }
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigoSku(rs.getString("codigo_sku"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        p.setStockActual(rs.getInt("stock_actual"));
        p.setStockReservado(rs.getInt("stock_reservado"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setCategoria(rs.getString("categoria"));
        int idProv = rs.getInt("id_proveedor");
        p.setIdProveedor(rs.wasNull() ? null : idProv);
        p.setEstado(rs.getBoolean("estado"));
        return p;
    }

    private void asignarTextoOpcional(PreparedStatement ps, int posicion, String texto) throws SQLException {
        if (texto == null || texto.trim().isEmpty()) {
            ps.setNull(posicion, Types.VARCHAR);
        } else {
            ps.setString(posicion, texto.trim());
        }
    }

    private void asignarEnteroOpcional(PreparedStatement ps, int posicion, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicion, Types.INTEGER);
        } else {
            ps.setInt(posicion, valor);
        }
    }
}
