package controlador;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import interfaces.ICategoriaDAO;
import interfaces.IProductoDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.Csrf;

/**
 * Catálogo público de la tienda en línea.
 * URL: /catalogo?q=filtro&categoria=1&soloStock=1
 */
@WebServlet(name = "CatalogoServlet", urlPatterns = {"/catalogo"})
public class CatalogoServlet extends HttpServlet {

    private final IProductoDAO productoDAO = new ProductoDAO();
    private final ICategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String texto = request.getParameter("q");
        Integer idCategoria = parsearEntero(request.getParameter("categoria"));
        boolean soloStock = "1".equals(request.getParameter("soloStock"));

        try {
            request.setAttribute("productos", productoDAO.listarCatalogo(texto, idCategoria, soloStock));
            request.setAttribute("categorias", categoriaDAO.listar());
        } catch (SQLException e) {
            log("Error al cargar el catálogo", e);
            request.setAttribute("error", "No se pudo cargar el catálogo. Intenta nuevamente en unos minutos.");
        }

        // Se devuelven los filtros para que el formulario los conserve
        request.setAttribute("q", texto == null ? "" : texto);
        request.setAttribute("categoriaSel", idCategoria);
        request.setAttribute("soloStock", soloStock);

        // Carrito: token para los formularios "Agregar" y mensajes que dejó CarritoServlet
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            if (sesion.getAttribute("usuario") != null) {
                request.setAttribute("csrf", Csrf.token(sesion));
            }
            moverMensaje(sesion, request, "flashOk");
            moverMensaje(sesion, request, "flashError");
        }

        request.getRequestDispatcher("/WEB-INF/vistas/publico/catalogo.jsp").forward(request, response);
    }

    private void moverMensaje(HttpSession sesion, HttpServletRequest request, String clave) {
        Object m = sesion.getAttribute(clave);
        if (m != null) {
            request.setAttribute(clave, m);
            sesion.removeAttribute(clave);
        }
    }

    private Integer parsearEntero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}