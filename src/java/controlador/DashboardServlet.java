package controlador;

import dao.DashboardDAO;
import interfaces.IDashboardDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Pantalla principal de la zona interna: resumen del negocio (ventas, stock, pedidos) y menú de módulos.
 * La seguridad la aplica AutenticacionFiltro (/interno/*), por eso aquí no se revisa la sesión.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/interno/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final IDashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("resumen", dashboardDAO.obtenerResumen());
        } catch (SQLException e) {
            // Si falla el resumen, el menú de módulos igual se muestra
            log("Error al cargar el resumen del panel", e);
            request.setAttribute("errorResumen", "No se pudo cargar el resumen. Los módulos siguen disponibles.");
        }
        request.getRequestDispatcher("/WEB-INF/vistas/interno/dashboard.jsp").forward(request, response);
    }
}
