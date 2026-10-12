/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import dao.PedidoDAO;
import entidad.Usuario;
import interfaces.IPedidoDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Historial de pedidos del cliente con sesión iniciada.
 * URL: /mis-pedidos
 */
@WebServlet(name = "MisPedidosServlet", urlPatterns = {"/mis-pedidos"})
public class MisPedidosServlet extends HttpServlet {

    private final IPedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession(false);
        Usuario usuario = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");
        String ctx = request.getContextPath();

        if (usuario == null) {
            response.sendRedirect(ctx + "/login");
            return;
        }
        if (usuario.isPersonal()) {
            // El personal gestiona los pedidos desde el panel interno
            response.sendRedirect(ctx + "/interno/pedidos");
            return;
        }

        try {
            // Se filtra por el usuario de la SESIÓN, nunca por un parámetro de la URL
            request.setAttribute("pedidos", pedidoDAO.listarPorUsuario(usuario.getIdUsuario()));
        } catch (SQLException e) {
            log("Error al listar los pedidos del cliente", e);
            request.setAttribute("error", "No se pudieron cargar sus pedidos. Intente nuevamente en unos minutos.");
        }

        request.getRequestDispatcher("/WEB-INF/vistas/publico/mis-pedidos.jsp").forward(request, response);
    }
}