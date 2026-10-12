package interfaces;

import entidad.Usuario;
import java.sql.SQLException;

public interface IClienteDAO {

    /**
     * true si ya hay un cliente con ese documento.
     */
    boolean existeDocumento(String documento) throws SQLException;

    /**
     * Crea la cuenta (usuario con rol CLIENTE) y su ficha de cliente en una
     * sola transacción: o se guardan las dos cosas o ninguna. Si el DNI ya
     * existía como cliente atendido en tienda (sin cuenta), se vincula a la
     * cuenta nueva.
     *
     * @param usuario con nombres, correo y contraseña ya hasheada
     * @param documento DNI
     * @param telefono puede ser null
     * @return false si el DNI ya pertenece a un cliente con cuenta
     */
    boolean registrarConCuenta(Usuario usuario, String documento, String telefono) throws SQLException;

    /**
     * id_cliente de la ficha asociada a esa cuenta, o null si la cuenta no
     * tiene ficha de cliente.
     */
    Integer buscarIdPorUsuario(int idUsuario) throws SQLException;
}
