package filtro;

import dao.UsuarioDAO;
import entidad.Usuario;
import interfaces.IUsuarioDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * GUARDIA DE LA ZONA INTERNA. Todo lo que cuelgue de /interno/* pasa por aquí.
 *
 *  1. Sin sesión                      -> al login del personal.
 *  2. Cliente (no es personal)        -> 403.
 *  3. Cuenta desactivada/eliminada    -> se cierra la sesión (se revisa en cada petición).
 *  4. Ruta solo de administrador      -> el vendedor recibe 403.
 *
 * PARA TUS COMPAÑEROS: si crean un módulo nuevo, basta con que su URL empiece con
 * /interno/. Si solo debe verlo el administrador, agréguenlo a RUTAS_SOLO_ADMIN.
 */
@WebFilter(filterName = "AutenticacionFiltro", urlPatterns = {"/interno/*"})
public class AutenticacionFiltro implements Filter {

    /** Rutas (prefijos) que solo puede abrir el ADMINISTRADOR. */
    private static final List<String> RUTAS_SOLO_ADMIN = Arrays.asList(
            "/interno/usuarios",
            "/interno/categorias",
            "/interno/reportes",
            "/interno/proveedores",
            "/interno/compras"
    );

    private final IUsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // Que el navegador no guarde estas páginas: al cerrar sesión y pulsar "atrás" no se ven
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession sesion = request.getSession(false);
        Usuario sesionUsuario = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");

        if (sesionUsuario == null) {
            irAlLogin(request, response);
            return;
        }
        if (!sesionUsuario.isPersonal()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el personal puede entrar aquí.");
            return;
        }

        // Se vuelve a consultar la BD: si el admin desactivó o cambió de rol a este
        // usuario, el cambio se aplica de inmediato aunque tenga la sesión abierta.
        try {
            Usuario actual = usuarioDAO.buscarPorId(sesionUsuario.getIdUsuario());
            if (actual == null || !actual.isEstado() || !actual.isPersonal()) {
                sesion.invalidate();
                irAlLogin(request, response);
                return;
            }
            actual.setContrasena(null);
            sesion.setAttribute("usuario", actual);
            sesionUsuario = actual;
        } catch (SQLException e) {
            request.getServletContext().log("No se pudo validar la sesión", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudo verificar su sesión. Intente nuevamente.");
            return;
        }

        if (!sesionUsuario.isAdministrador() && esRutaSoloAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Esta sección es solo para el administrador.");
            return;
        }

        chain.doFilter(req, resp);
    }

    private boolean esRutaSoloAdmin(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        for (String prefijo : RUTAS_SOLO_ADMIN) {
            if (ruta.equals(prefijo) || ruta.startsWith(prefijo + "/")) {
                return true;
            }
        }
        return false;
    }

    private void irAlLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login?tipo=personal");
    }
}
