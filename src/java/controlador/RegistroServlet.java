/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ClienteDAO;
import dao.UsuarioDAO;
import entidad.Usuario;
import interfaces.IClienteDAO;
import interfaces.IUsuarioDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.SeguridadClave;
import util.Validador;

/**
 * Registro de clientes de la tienda en línea.
 * URL: /registro
 *
 * Crea el usuario (rol CLIENTE, fijado por el DAO, nunca por el formulario) y su ficha
 * de cliente en una sola transacción. Si el DNI ya existía como cliente atendido en
 * tienda (sin cuenta), se vincula a la cuenta nueva.
 */
@WebServlet(name = "RegistroServlet", urlPatterns = {"/registro"})
public class RegistroServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/publico/registro.jsp";

    private final IUsuarioDAO usuarioDAO = new UsuarioDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Si ya inició sesión, no tiene sentido mostrar el registro
        HttpSession sesion = request.getSession(false);
        Usuario actual = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");
        if (actual != null) {
            response.sendRedirect(request.getContextPath()
                    + (actual.isPersonal() ? "/interno/dashboard" : "/inicio"));
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nombres = limpiar(request.getParameter("nombres"));
        String documento = limpiar(request.getParameter("documento"));
        String telefono = limpiar(request.getParameter("telefono"));
        String correo = limpiar(request.getParameter("correo")).toLowerCase();
        String clave = request.getParameter("clave") == null ? "" : request.getParameter("clave");
        String clave2 = request.getParameter("clave2") == null ? "" : request.getParameter("clave2");

        // Para que el formulario conserve lo escrito si hay un error (las contraseñas nunca se devuelven)
        request.setAttribute("nombres", nombres);
        request.setAttribute("documento", documento);
        request.setAttribute("telefono", telefono);
        request.setAttribute("correo", correo);

        // 1) Validaciones de formato
        String error = null;
        if (!Validador.nombreValido(nombres)) {
            error = "Ingrese sus nombres y apellidos (mínimo 3 caracteres).";
        } else if (!Validador.dniValido(documento)) {
            error = "El DNI debe tener 8 dígitos.";
        } else if (!telefono.isEmpty() && !Validador.telefonoValido(telefono)) {
            error = "El teléfono debe tener 9 dígitos.";
        } else if (!Validador.correoValido(correo)) {
            error = "Ingrese un correo electrónico válido.";
        } else if (!Validador.claveValida(clave)) {
            error = Validador.MENSAJE_CLAVE;
        } else if (!clave.equals(clave2)) {
            error = "Las contraseñas no coinciden.";
        }
        if (error != null) {
            mostrarError(request, response, error);
            return;
        }

        // 2) Registro
        try {
            if (usuarioDAO.buscarPorCorreo(correo) != null) {
                mostrarError(request, response, "Ya existe una cuenta con ese correo.");
                return;
            }

            Usuario u = new Usuario();
            u.setNombres(nombres);
            u.setCorreo(correo);
            u.setContrasena(SeguridadClave.hashear(clave));   // nunca se guarda en texto plano

            boolean creada = clienteDAO.registrarConCuenta(u, documento, telefono.isEmpty() ? null : telefono);
            if (!creada) {
                mostrarError(request, response, "Ese DNI ya tiene una cuenta registrada.");
                return;
            }

            // Cuenta creada: al login, que muestra "Cuenta creada correctamente"
            response.sendRedirect(request.getContextPath() + "/login?registrado=1");

        } catch (SQLIntegrityConstraintViolationException e) {
            // Otra persona registró el mismo correo o DNI justo en este instante
            mostrarError(request, response, "Ya existe una cuenta con ese correo o ese DNI.");
        } catch (SQLException e) {
            log("Error al registrar el cliente", e);
            mostrarError(request, response, "No se pudo crear la cuenta. Intente nuevamente en unos minutos.");
        }
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
