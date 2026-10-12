/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import entidad.Categoria;
import java.sql.SQLException;
import java.util.List;

public interface ICategoriaDAO {

    /**
     * Lista simple (para selectores y el catálogo público).
     */
    List<Categoria> listar() throws SQLException;

    /**
     * Lista con la cantidad de productos de cada categoría (pantalla de
     * administración).
     */
    List<Categoria> listarConTotales() throws SQLException;

    Categoria buscarPorId(int idCategoria) throws SQLException;

    Categoria buscarPorNombre(String nombre) throws SQLException;

    boolean registrar(Categoria categoria) throws SQLException;

    boolean actualizar(Categoria categoria) throws SQLException;

    /**
     * Solo debe llamarse si la categoría no tiene productos.
     */
    boolean eliminar(int idCategoria) throws SQLException;

    int contarProductos(int idCategoria) throws SQLException;
}
