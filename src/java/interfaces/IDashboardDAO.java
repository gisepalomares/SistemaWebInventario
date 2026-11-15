package interfaces;

import entidad.ResumenPanel;
import java.sql.SQLException;

public interface IDashboardDAO {

    /** Ventas de hoy y del mes, alertas de stock y pedidos en curso (solo lectura). */
    ResumenPanel obtenerResumen() throws SQLException;
}
