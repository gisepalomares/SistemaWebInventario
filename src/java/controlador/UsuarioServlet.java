package controlador;

import dao.UsuarioDAO;
import entidad.Usuario;
import interfaces.IUsuarioDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.Csrf;
import util.Roles;
import util.SeguridadClave;
import util.Validador;

/**
 * Gestión de cuentas del PERSONAL (solo administrador). URL: /interno/usuarios
 * El acceso solo-admin lo garantiza AutenticacionFiltro (RUTAS_SOLO_ADMIN).
 *
 * Acciones (POST, campo "accion"): crear | editar | estado | clave
 *
 * Este servlet sirve de MODELO para los demás módulos:
 *   doGet  -> carga datos con el DAO y muestra la vista JSP
 *   doPost -> valida, llama al DAO y redirige (patrón Post-Redirect-Get)
 *             dejando un mensaje en la sesión ("flash").
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/interno/usuarios"})
public class UsuarioServlet extends HttpServlet {

    private final IUsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession();

        // Mensaje de la operación anterior (se muestra una sola vez)
        for (String clave : new String[]{"flashOk", "flashError"}) {
            Object valor = sesion.getAttribute(clave);
            if (valor != null) {
                request.setAttribute(clave.equals("flashOk") ? "ok" : "error", valor);
                sesion.removeAttribute(clave);
            }
        }

        try {
            request.setAttribute("usuarios", usuarioDAO.listarPersonal());
        } catch (SQLException e) {
            log("Error al listar usuarios", e);
            request.setAttribute("error", "No se pudo cargar la lista de usuarios.");
        }
        request.setAttribute("csrf", Csrf.token(sesion));
        request.getRequestDispatcher("/WEB-INF/vistas/interno/usuarios.jsp").forward(request, response);
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

        Usuario yo = (Usuario) sesion.getAttribute("usuario");
        String accion = limpiar(request.getParameter("accion"));
        String mensajeOk = null;
        String mensajeError;

        try {
            switch (accion) {
                case "crear":
                    mensajeError = crear(request);
                    mensajeOk = "Usuario creado. Entréguele su correo y la contraseña temporal.";
                    break;
                case "editar":
                    mensajeError = editar(request, yo);
                    mensajeOk = "Usuario actualizado.";
                    break;
                case "estado":
                    mensajeError = cambiarEstado(request, yo);
                    mensajeOk = "Estado de la cuenta actualizado.";
                    break;
                case "clave":
                    mensajeError = restablecerClave(request);
                    mensajeOk = "Contraseña restablecida. Entregue la nueva contraseña al usuario.";
                    break;
                default:
                    mensajeError = "Acción no reconocida.";
            }
        } catch (SQLException e) {
            log("Error en gestión de usuarios (" + accion + ")", e);
            mensajeError = "Ocurrió un error con la base de datos. Intente nuevamente.";
        }

        if (mensajeError != null) {
            sesion.setAttribute("flashError", mensajeError);
        } else {
            sesion.setAttribute("flashOk", mensajeOk);
        }
        response.sendRedirect(request.getContextPath() + "/interno/usuarios");
    }

    // Cada método devuelve null si todo salió bien, o el mensaje de error.

    private String crear(HttpServletRequest request) throws SQLException {
        String nombres = limpiar(request.getParameter("nombres"));
        String correo = limpiar(request.getParameter("correo")).toLowerCase();
        String rol = limpiar(request.getParameter("rol"));
        String clave = request.getParameter("clave") == null ? "" : request.getParameter("clave");

        if (!Validador.nombreValido(nombres)) {
            return "Ingrese los nombres del usuario (mínimo 3 caracteres).";
        }
        if (!Validador.correoValido(correo)) {
            return "Ingrese un correo válido.";
        }
        if (!rolDePersonalValido(rol)) {
            return "Seleccione un rol válido (Administrador o Vendedor).";
        }
        if (!Validador.claveValida(clave)) {
            return Validador.MENSAJE_CLAVE;
        }
        if (usuarioDAO.buscarPorCorreo(correo) != null) {
            return "Ya existe un usuario con ese correo.";
        }

        Usuario u = new Usuario();
        u.setNombres(nombres);
        u.setCorreo(correo);
        u.setContrasena(SeguridadClave.hashear(clave));
        u.setRol(rol);
        u.setEstado(true);
        return usuarioDAO.registrar(u) ? null : "No se pudo crear el usuario.";
    }

    private String editar(HttpServletRequest request, Usuario yo) throws SQLException {
        Usuario objetivo = objetivoDelPersonal(request);
        if (objetivo == null) {
            return "El usuario no existe o no pertenece al personal.";
        }
        String nombres = limpiar(request.getParameter("nombres"));
        // Si se edita a sí mismo, el rol no se toca (el campo viaja deshabilitado en la pantalla)
        String rol = (objetivo.getIdUsuario() == yo.getIdUsuario())
                ? objetivo.getRol() : limpiar(request.getParameter("rol"));

        if (!Validador.nombreValido(nombres)) {
            return "Ingrese los nombres del usuario (mínimo 3 caracteres).";
        }
        if (!rolDePersonalValido(rol)) {
            return "Seleccione un rol válido.";
        }
        boolean cambiaRol = !rol.equals(objetivo.getRol());
        if (cambiaRol && objetivo.getIdUsuario() == yo.getIdUsuario()) {
            return "No puede cambiar su propio rol.";
        }
        if (cambiaRol && objetivo.isAdministrador() && objetivo.isEstado()
                && usuarioDAO.contarAdministradoresActivos() <= 1) {
            return "Debe quedar al menos un administrador activo.";
        }
        return usuarioDAO.actualizar(objetivo.getIdUsuario(), nombres, rol)
                ? null : "No se pudo actualizar el usuario.";
    }

    private String cambiarEstado(HttpServletRequest request, Usuario yo) throws SQLException {
        Usuario objetivo = objetivoDelPersonal(request);
        if (objetivo == null) {
            return "El usuario no existe o no pertenece al personal.";
        }
        boolean activar = "1".equals(request.getParameter("activo"));

        if (!activar) {
            if (objetivo.getIdUsuario() == yo.getIdUsuario()) {
                return "No puede desactivar su propia cuenta.";
            }
            if (objetivo.isAdministrador() && objetivo.isEstado()
                    && usuarioDAO.contarAdministradoresActivos() <= 1) {
                return "Debe quedar al menos un administrador activo.";
            }
        }
        return usuarioDAO.cambiarEstado(objetivo.getIdUsuario(), activar)
                ? null : "No se pudo cambiar el estado.";
    }

    private String restablecerClave(HttpServletRequest request) throws SQLException {
        Usuario objetivo = objetivoDelPersonal(request);
        if (objetivo == null) {
            return "El usuario no existe o no pertenece al personal.";
        }
        String clave = request.getParameter("clave") == null ? "" : request.getParameter("clave");
        if (!Validador.claveValida(clave)) {
            return Validador.MENSAJE_CLAVE;
        }
        return usuarioDAO.cambiarClave(objetivo.getIdUsuario(), SeguridadClave.hashear(clave))
                ? null : "No se pudo cambiar la contraseña.";
    }

    /** Busca el usuario del formulario y confirma que sea del personal (nunca un cliente). */
    private Usuario objetivoDelPersonal(HttpServletRequest request) throws SQLException {
        try {
            int id = Integer.parseInt(limpiar(request.getParameter("id")));
            Usuario u = usuarioDAO.buscarPorId(id);
            return (u != null && u.isPersonal()) ? u : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean rolDePersonalValido(String rol) {
        return Roles.ADMINISTRADOR.equals(rol) || Roles.VENDEDOR.equals(rol);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
