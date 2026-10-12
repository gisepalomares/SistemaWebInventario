/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import entidad.Proveedor;
import java.sql.SQLException;
import java.util.List;

public interface IProveedorDAO {

    List<Proveedor> listar() throws SQLException;

    Proveedor buscarPorId(int idProveedor) throws SQLException;
}
