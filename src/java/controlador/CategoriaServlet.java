import dao.CategoriaDAO;
import entidad.Categoria;
import interfaces.ICategoriaDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.Csrf;

/**
 * Categorías de los productos (solo administrador). URL: /interno/categorias
 * El acceso solo-admin lo garantiza AutenticacionFiltro (RUTAS_SOLO_ADMIN).
 *
 * Acciones (POST, campo "accion"): crear | editar | eliminar
 * Una categoría NO se puede eliminar mientras tenga productos (activos o inactivos).
 */
@WebServlet(name = "CategoriaServlet", urlPatterns = {"/interno/categorias"})
public class CategoriaServlet extends HttpServlet {

    private static final String[] CAMPOS_FORMULARIO = {"id", "nombre", "descripcion"};

    private final ICategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession();
        recuperarMensajes(sesion, request);

        try {
            request.setAttribute("categorias", categoriaDAO.listarConTotales());
        } catch (SQLException e) {
            log("Error al listar categorías", e);
            request.setAttribute("error", "No se pudo cargar la lista de categorías.");
        }
        request.setAttribute("csrf", Csrf.token(sesion));
        request.getRequestDispatcher("/WEB-INF/vistas/interno/categorias.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession sesion = request.getSession();

        if (!Csrf.valido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida. Recargue la página.");
            return;
        }

        String accion = limpiar(request.getParameter("accion"));
        String mensajeOk = null;
        String mensajeError;

        try {
            switch (accion) {
                case "crear":
                    mensajeError = crear(request);
                    mensajeOk = "Categoría registrada correctamente.";
                    break;
                case "editar":
                    mensajeError = editar(request);
                    mensajeOk = "Categoría actualizada correctamente.";
                    break;
                case "eliminar":
                    mensajeError = eliminar(request);
                    mensajeOk = "Categoría eliminada.";
                    break;
                default:
                    mensajeError = "Acción no reconocida.";
            }
        } catch (SQLException e) {
            log("Error en gestión de categorías (" + accion + ")", e);
            if (e.getErrorCode() == 1062) {          // nombre duplicado
                mensajeError = "Ya existe una categoría con ese nombre.";
            } else if (e.getErrorCode() == 1451) {   // tiene productos asociados
                mensajeError = "No se puede eliminar: la categoría tiene productos asociados.";
            } else {
                mensajeError = "Ocurrió un error con la base de datos. Intente nuevamente.";
            }
        }

        if (mensajeError != null) {
            sesion.setAttribute("flashError", mensajeError);
            if ("crear".equals(accion) || "editar".equals(accion)) {
                guardarFormulario(sesion, request, accion);
            }
        } else {
            sesion.setAttribute("flashOk", mensajeOk);
        }
        response.sendRedirect(request.getContextPath() + "/interno/categorias");
    }

    private String crear(HttpServletRequest request) throws SQLException {
        Categoria c = new Categoria();
        String error = leerYValidar(request, c);
        if (error != null) {
            return error;
        }
        return categoriaDAO.registrar(c) ? null : "No se pudo registrar la categoría.";
    }

    private String editar(HttpServletRequest request) throws SQLException {
        Integer id = entero(request.getParameter("id"));
        if (id == null || categoriaDAO.buscarPorId(id) == null) {
            return "La categoría no existe.";
        }
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        String error = leerYValidar(request, c);
        if (error != null) {
            return error;
        }
        return categoriaDAO.actualizar(c) ? null : "No se pudo actualizar la categoría.";
    }

    private String eliminar(HttpServletRequest request) throws SQLException {
        Integer id = entero(request.getParameter("id"));
        if (id == null || categoriaDAO.buscarPorId(id) == null) {
            return "La categoría no existe.";
        }
        int total = categoriaDAO.contarProductos(id);
        if (total > 0) {
            return "No se puede eliminar: la categoría tiene " + total
                    + " producto(s). Primero cambie de categoría a esos productos.";
        }
        return categoriaDAO.eliminar(id) ? null : "No se pudo eliminar la categoría.";
    }

    private String leerYValidar(HttpServletRequest request, Categoria c) throws SQLException {
        String nombre = limpiar(request.getParameter("nombre"));
        String descripcion = limpiar(request.getParameter("descripcion"));

        if (nombre.length() < 3 || nombre.length() > 60) {
            return "Ingrese el nombre de la categoría (de 3 a 60 caracteres).";
        }
        if (descripcion.length() > 200) {
            return "La descripción no puede superar los 200 caracteres.";
        }
        Categoria conEseNombre = categoriaDAO.buscarPorNombre(nombre);
        if (conEseNombre != null && conEseNombre.getIdCategoria() != c.getIdCategoria()) {
            return "Ya existe una categoría con ese nombre.";
        }
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        return null;
    }

    private void recuperarMensajes(HttpSession sesion, HttpServletRequest request) {
        String[][] pares = {{"flashOk", "ok"}, {"flashError", "error"},
        {"flashForm", "form"}, {"flashFormAccion", "formAccion"}};
        for (String[] par : pares) {
            Object valor = sesion.getAttribute(par[0]);
            if (valor != null) {
                request.setAttribute(par[1], valor);
                sesion.removeAttribute(par[0]);
            }
        }
    }

    private void guardarFormulario(HttpSession sesion, HttpServletRequest request, String accion) {
        Map<String, String> formulario = new HashMap<>();
        for (String campo : CAMPOS_FORMULARIO) {
            String valor = request.getParameter(campo);
            formulario.put(campo, valor == null ? "" : valor);
        }
        sesion.setAttribute("flashForm", formulario);
        sesion.setAttribute("flashFormAccion", accion);
    }

    private Integer entero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}