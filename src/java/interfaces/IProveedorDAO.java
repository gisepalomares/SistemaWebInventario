/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;
import entidad.Proveedor;
import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author Alex
 */
public interface IProveedorDAO {
    // Obtener todos los proveedores
    List<Proveedor> listar() throws SQLException;

    // Buscar proveedor por ID
    Proveedor buscarPorId(int idProveedor)
            throws SQLException;

    // Buscar proveedores por RUC o razón social
    List<Proveedor> buscar(String texto)
            throws SQLException;

    // Registrar un nuevo proveedor
    boolean registrar(Proveedor proveedor)
            throws SQLException;

    // Actualizar los datos de un proveedor
    boolean actualizar(Proveedor proveedor)
            throws SQLException;
    
    // Eliminar los datos de un proveedor
    boolean eliminar(int idProveedor) 
            throws SQLException;
}
