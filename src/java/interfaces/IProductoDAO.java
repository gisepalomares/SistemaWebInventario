/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import entidad.Producto;
import java.sql.SQLException;
import java.util.List;

public interface IProductoDAO {

    // ------------------------------------------------------------------
    //  Catálogo público (solo productos activos)
    // ------------------------------------------------------------------
    /**
     * Lista los productos activos para el catálogo.
     *
     * @param texto texto a buscar en nombre o SKU (null o vacío = sin filtro)
     * @param idCategoria categoría a filtrar (null = todas)
     * @param soloConStock true = oculta los productos agotados
     */
    List<Producto> listarCatalogo(String texto, Integer idCategoria, boolean soloConStock) throws SQLException;

    Producto buscarPorId(int idProducto) throws SQLException;

    // ------------------------------------------------------------------
    //  Zona interna (gestión de productos)
    // ------------------------------------------------------------------
    /**
     * Lista de gestión: incluye el nombre del proveedor y puede incluir
     * inactivos.
     *
     * @param texto texto a buscar en nombre o SKU (null o vacío = sin filtro)
     * @param idCategoria categoría a filtrar (null = todas)
     * @param estado "activos", "inactivos" o "todos"
     */
    List<Producto> listarGestion(String texto, Integer idCategoria, String estado) throws SQLException;

    /**
     * Busca por id SIN importar si está activo o inactivo.
     */
    Producto obtenerParaGestion(int idProducto) throws SQLException;

    /**
     * Busca por código SKU (activo o inactivo).
     */
    Producto buscarPorSku(String codigoSku) throws SQLException;

    /**
     * Inserta un producto nuevo (con su stock inicial).
     */
    boolean registrar(Producto producto) throws SQLException;

    /**
     * Actualiza los datos editables. NO toca el stock (se mueve con compras y
     * ventas).
     */
    boolean actualizar(Producto producto) throws SQLException;

    boolean cambiarEstado(int idProducto, boolean activo) throws SQLException;
}
