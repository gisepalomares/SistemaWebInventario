/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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
import util.SeguridadClave;

/**
 * Inicio de sesión (personal y clientes).
 * URL: /login            -> acceso general
 *      /login?tipo=personal -> acceso del personal (administrador / vendedor)
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final String VISTA_LOGIN = "/WEB-INF/vistas/publico/login.jsp";
    private static final int MINUTOS_INACTIVIDAD = 30;

    /** Hash de mentira: se usa cuando el correo no existe, para que la respuesta tarde lo mismo. */
    private static final String HASH_FALSO = SeguridadClave.hashear("clave-falsa-sin-uso");

    private final IUsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Si ya inició sesión, no tiene sentido mostrarle el formulario otra vez
        Usuario actual = usuarioEnSesion(request);
        if (actual != null) {
            response.sendRedirect(request.getContextPath() + destinoSegunRol(actual));
            return;
        }
        request.setAttribute("tipo", "personal".equals(request.getParameter("tipo")) ? "personal" : "");
        if ("1".equals(request.getParameter("registrado"))) {
            request.setAttribute("ok", "Cuenta creada correctamente. Ya puede iniciar sesión.");
        }
        request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String correo = limpiar(request.getParameter("correo")).toLowerCase();
        String clave = request.getParameter("clave") == null ? "" : request.getParameter("clave");
        boolean accesoPersonal = "personal".equals(request.getParameter("tipo"));

        if (correo.isEmpty() || clave.isEmpty()) {
            mostrarError(request, response, "Ingrese su correo y su contraseña.", correo, accesoPersonal);
            return;
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorCorreo(correo);

            // Se verifica la clave SIEMPRE (aunque el correo no exista) y se da un
            // mensaje genérico: así no se revela qué correos están registrados.
            String hash = (usuario != null) ? usuario.getContrasena() : HASH_FALSO;
            boolean claveCorrecta = SeguridadClave.verificar(clave, hash);

            if (usuario == null || !usuario.isEstado() || !claveCorrecta) {
                mostrarError(request, response, "Correo o contraseña incorrectos.", correo, accesoPersonal);
                return;
            }
            if (accesoPersonal && !usuario.isPersonal()) {
                mostrarError(request, response, "Esta cuenta no tiene acceso al panel del personal.",
                        correo, true);
                return;
            }

            iniciarSesion(request, usuario);
            response.sendRedirect(request.getContextPath() + destinoSegunRol(usuario));

        } catch (SQLException e) {
            log("Error al iniciar sesión", e);
            mostrarError(request, response, "No se pudo iniciar sesión. Intente nuevamente.",
                    correo, accesoPersonal);
        }
    }

    private void iniciarSesion(HttpServletRequest request, Usuario usuario) {
        // Se descarta la sesión anterior y se crea una nueva (evita "session fixation")
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = request.getSession(true);

        usuario.setContrasena(null);   // el hash nunca debe viajar en la sesión
        sesion.setAttribute("usuario", usuario);
        sesion.setMaxInactiveInterval(MINUTOS_INACTIVIDAD * 60);
    }

    private String destinoSegunRol(Usuario usuario) {
        return usuario.isPersonal() ? "/interno/dashboard" : "/inicio";
    }

    private Usuario usuarioEnSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response,
            String mensaje, String correo, boolean accesoPersonal) throws ServletException, IOException {

        request.setAttribute("error", mensaje);
        request.setAttribute("correo", correo);
        request.setAttribute("tipo", accesoPersonal ? "personal" : "");
        request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
