package controlador;

import entidad.Comprobante;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import servicio.PedidoFacade;

/**
 * Muestra la captura de pago (Yape/Plin) que subió el cliente. URL: /interno/comprobante?id=NN
 * (NN = id del pago). Al colgar de /interno/*, AutenticacionFiltro garantiza que solo el personal
 * (administrador o vendedor) con sesión activa pueda verla: el cliente no tiene acceso.
 */
@WebServlet(name = "ComprobanteServlet", urlPatterns = {"/interno/comprobante"})
public class ComprobanteServlet extends HttpServlet {

    private final PedidoFacade facade = new PedidoFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id").trim());
        } catch (RuntimeException e) {          // sin id, vacío o no numérico
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Comprobante c = facade.obtenerComprobante(id);
            if (c == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType(c.getTipo());
            response.setContentLength(c.getDatos().length);
            // Se muestra como imagen y nada más (sin que el navegador "adivine" otro tipo)
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Security-Policy", "default-src 'none'; sandbox");
            response.setHeader("Content-Disposition", "inline; filename=\"comprobante-" + id + "\"");
            response.getOutputStream().write(c.getDatos());
        } catch (SQLException e) {
            log("Error al leer el comprobante " + id, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
