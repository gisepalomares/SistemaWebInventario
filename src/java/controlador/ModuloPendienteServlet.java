package controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MARCADOR DE POSICIÓN para los módulos que aún no se desarrollan.
 * Muestra una pantalla "en construcción" con la plantilla interna ya aplicada.
 *
 * PARA TUS COMPAÑEROS: al crear el servlet real de su módulo (por ejemplo
 * ProductoServlet con urlPatterns "/interno/productos"), BORREN esa URL de la
 * lista de abajo. Si la dejan repetida, Tomcat no arranca por URL duplicada.
 * Usen como modelo la pantalla de usuarios.jsp / UsuarioServlet.
 */
@WebServlet(name = "ModuloPendienteServlet", urlPatterns = {
    "/interno/compras",
    "/interno/reportes"
})
public class ModuloPendienteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ruta = request.getServletPath();
        String nombre;
        String icono;
        switch (ruta) {
            case "/interno/productos":
                nombre = "Productos"; icono = "bi-box-seam"; break;
            case "/interno/categorias":
                nombre = "Categorías"; icono = "bi-tags"; break;
            case "/interno/ventas":
                nombre = "Ventas"; icono = "bi-receipt"; break;
            case "/interno/pedidos":
                nombre = "Pedidos en línea"; icono = "bi-bag-check"; break;
            case "/interno/proveedores":
                nombre = "Proveedores"; icono = "bi-truck"; break;
            case "/interno/compras":
                nombre = "Compras"; icono = "bi-cart-plus"; break;
            default:
                nombre = "Reportes"; icono = "bi-bar-chart-line"; break;
        }
        request.setAttribute("moduloNombre", nombre);
        request.setAttribute("moduloIcono", icono);
        request.setAttribute("moduloRuta", ruta.substring("/interno/".length()));
        request.getRequestDispatcher("/WEB-INF/vistas/interno/modulo-pendiente.jsp").forward(request, response);
    }
}
