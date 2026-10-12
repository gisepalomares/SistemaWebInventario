package controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Pantalla principal de la zona interna (menú de módulos).
 * La seguridad la aplica AutenticacionFiltro (/interno/*), por eso aquí no se revisa la sesión.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/interno/dashboard"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/interno/dashboard.jsp").forward(request, response);
    }
}
