/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import dao.ProductoDAO;
import interfaces.IProductoDAO;
import dao.VentaDAO;
import entidad.DetalleVenta;
import entidad.Usuario;
import entidad.Venta;
import interfaces.IVentaDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.Csrf;
/**
 *
 * @author Alex
 */
@WebServlet(name = "VentaServlet", urlPatterns = {"/interno/ventas"})
public class VentaServlet extends HttpServlet {

    private final IVentaDAO ventaDAO = new VentaDAO();
    private final IProductoDAO productoDAO = new ProductoDAO();
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("ventas", ventaDAO.listar());
            request.setAttribute("productos",
            productoDAO.listarCatalogo(null, null, true));
            String idVentaTexto = request.getParameter("idVenta");
            
            if (idVentaTexto != null && !idVentaTexto.trim().isEmpty()) {
                try {
                    int idVenta = Integer.parseInt(idVentaTexto);

                    if (idVenta <= 0) {
                        response.sendError(400, "ID de venta no válido.");
                        return;
                    }

                    Venta ventaSeleccionada = ventaDAO.buscarPorId(idVenta);

                    if (ventaSeleccionada == null) {
                        response.sendError(404, "La venta no existe.");
                        return;
                    }

                    request.setAttribute("ventaSeleccionada", ventaSeleccionada);
                    request.setAttribute("detallesVenta",
                            ventaDAO.listarDetalles(idVenta));

                } catch (NumberFormatException e) {
                    response.sendError(400, "ID de venta no válido.");
                    return;
                }
            }
            
            HttpSession sesion = request.getSession(false);
            request.setAttribute("csrf", Csrf.token(sesion));

            request.getRequestDispatcher(
                    "/WEB-INF/vistas/interno/ventas.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log("Error al listar ventas", e);
            response.sendError(500, "No se pudieron cargar las ventas.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!Csrf.valido(request)) {
            response.sendError(403, "Solicitud no válida.");
            return;
        }

        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || !(sesion.getAttribute("usuario") instanceof Usuario)) {
            response.sendError(401, "Debe iniciar sesión.");
            return;
        }

        Usuario usuario = (Usuario) sesion.getAttribute("usuario");

        try {
            String comprobante = request.getParameter("tipoComprobante");
            String[] ids = request.getParameterValues("idProducto");
            String[] cantidades = request.getParameterValues("cantidad");

            if (ids == null || cantidades == null
                    || ids.length == 0 || ids.length != cantidades.length) {
                throw new IllegalArgumentException(
                        "Debe seleccionar al menos un producto.");
            }

            Venta venta = new Venta();
            venta.setIdUsuario(usuario.getIdUsuario());
            venta.setTipoComprobante(comprobante);

            List<DetalleVenta> detalles = new ArrayList<>();

            for (int i = 0; i < ids.length; i++) {
                DetalleVenta detalle = new DetalleVenta();
                detalle.setIdProducto(Integer.parseInt(ids[i]));
                detalle.setCantidad(Integer.parseInt(cantidades[i]));
                detalles.add(detalle);
            }

            int idVenta = ventaDAO.registrar(venta, detalles);

            sesion.setAttribute("mensajeVenta",
                    "Venta N.º " + idVenta + " registrada correctamente.");

        } catch (SQLException | IllegalArgumentException e) {
            getServletContext().log("Error al registrar venta", e);
            sesion.setAttribute("errorVenta",
                    "No se pudo registrar la venta: " + e.getMessage());
        }

        response.sendRedirect(
                request.getContextPath() + "/interno/ventas");
    }
}