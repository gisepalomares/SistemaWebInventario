/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import entidad.Carrito;
import entidad.Pedido;
import entidad.Usuario;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import servicio.PedidoFacade;
import util.Csrf;
import util.PedidoException;

/**
 * POST /pedido/confirmar  -> confirma el pedido (reserva el stock) y redirige a la pantalla de éxito.
 * GET  /pedido/confirmado -> muestra el código de recojo del pedido recién creado.
 */
@WebServlet(name = "ConfirmarPedidoServlet", urlPatterns = {"/pedido/confirmar", "/pedido/confirmado"})
public class ConfirmarPedidoServlet extends HttpServlet {

    private static final String VISTA_CONFIRMADO = "/WEB-INF/vistas/publico/pedido-confirmado.jsp";

    private final PedidoFacade facade = new PedidoFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (clienteEnSesion(request, response) == null) {
            return;
        }
        HttpSession sesion = request.getSession();
        String ctx = request.getContextPath();

        if ("/pedido/confirmado".equals(request.getServletPath())) {
            if (sesion.getAttribute("pedidoConfirmado") != null) {
                request.getRequestDispatcher(VISTA_CONFIRMADO).forward(request, response);
            } else {
                response.sendRedirect(ctx + "/catalogo");
            }
            return;
        }
        response.sendRedirect(ctx + "/carrito");      // GET /pedido/confirmar no hace nada
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!"/pedido/confirmar".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Usuario usuario = clienteEnSesion(request, response);
        if (usuario == null) {
            return;
        }
        if (!Csrf.valido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solicitud no válida. Recargue la página e intente de nuevo.");
            return;
        }

        HttpSession sesion = request.getSession();
        String ctx = request.getContextPath();
        Carrito carrito = (Carrito) sesion.getAttribute("carrito");
        if (carrito == null || carrito.isVacio()) {
            sesion.setAttribute("flashError", "Su carrito está vacío.");
            response.sendRedirect(ctx + "/carrito");
            return;
        }

        String metodoPago = request.getParameter("metodoPago");
        boolean simularRechazo = "RECHAZADO".equals(request.getParameter("resultadoSimulado"));

        // synchronized: un doble clic no puede crear dos pedidos con el mismo carrito
        synchronized (carrito) {
            if (carrito.isVacio()) {
                response.sendRedirect(ctx + "/carrito");
                return;
            }
            try {
                Pedido pedido = facade.confirmarPedido(usuario, carrito, metodoPago, simularRechazo);
                carrito.vaciar();
                sesion.setAttribute("pedidoConfirmado", pedido);
                response.sendRedirect(ctx + "/pedido/confirmado");
            } catch (PedidoException e) {
                sesion.setAttribute("flashError", e.getMessage());
                response.sendRedirect(ctx + "/carrito");
            } catch (SQLException e) {
                log("Error al confirmar el pedido", e);
                sesion.setAttribute("flashError", "No se pudo registrar su pedido. Intente nuevamente.");
                response.sendRedirect(ctx + "/carrito");
            }
        }
    }

    /** Solo clientes con sesión. Si no, redirige y devuelve null. */
    private Usuario clienteEnSesion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession sesion = request.getSession(false);
        Usuario u = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");

        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (u.isPersonal()) {
            sesion.setAttribute("flashError", "El personal no realiza pedidos en línea. Use una cuenta de cliente.");
            response.sendRedirect(request.getContextPath() + "/catalogo");
            return null;
        }
        return u;
    }
}