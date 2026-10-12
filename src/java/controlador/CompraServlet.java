package controlador;

import dao.CompraDAO;
import dao.ProductoDAO;
import dao.ProveedorDAO;
import entidad.Compra;
import entidad.DetalleCompra;
import interfaces.ICompraDAO;
import interfaces.IProductoDAO;
import interfaces.IProveedorDAO;
import java.io.IOException;
import java.math.BigDecimal;
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
 * Compras a proveedores (entradas de mercadería). Solo administrador:
 * lo garantiza AutenticacionFiltro (RUTAS_SOLO_ADMIN).
 *
 * Acciones (POST, campo "accion"): registrar | anular
 *
 * @author Diego
 */
@WebServlet(name = "CompraServlet", urlPatterns = {"/interno/compras"})
public class CompraServlet extends HttpServlet {

    private final ICompraDAO compraDAO = new CompraDAO();
    private final IProveedorDAO proveedorDAO = new ProveedorDAO();
    private final IProductoDAO productoDAO = new ProductoDAO();

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
            request.setAttribute("compras", compraDAO.listar());
            request.setAttribute("proveedores", proveedorDAO.listar());
            // Todos los productos activos, aunque no tengan stock (justo esos se compran)
            request.setAttribute("productos", productoDAO.listarCatalogo(null, null, false));

            String idTexto = request.getParameter("idCompra");
            if (idTexto != null && !idTexto.trim().isEmpty()) {
                Integer idCompra = entero(idTexto);
                Compra compra = (idCompra == null) ? null : compraDAO.buscarPorId(idCompra);

                if (compra == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "La compra no existe.");
                    return;
                }
                request.setAttribute("compraSeleccionada", compra);
                request.setAttribute("detallesCompra", compraDAO.listarDetalles(idCompra));
            }
        } catch (SQLException e) {
            log("Error al listar compras", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudieron cargar las compras.");
            return;
        }

        request.setAttribute("csrf", Csrf.token(sesion));
        request.getRequestDispatcher("/WEB-INF/vistas/interno/compras.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!Csrf.valido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida. Recargue la página.");
            return;
        }

        HttpSession sesion = request.getSession();
        String accion = request.getParameter("accion");

        try {
            if ("registrar".equals(accion)) {
                int idCompra = registrar(request);
                sesion.setAttribute("flashOk", "Compra N.° " + idCompra
                        + " registrada. Se sumó la mercadería al stock.");

            } else if ("anular".equals(accion)) {
                Integer idCompra = entero(request.getParameter("idCompra"));
                if (idCompra == null) {
                    throw new IllegalArgumentException("Compra no válida.");
                }
                compraDAO.anular(idCompra);
                sesion.setAttribute("flashOk", "Compra N.° " + idCompra
                        + " anulada. Se descontó la mercadería del stock.");

            } else {
                throw new IllegalArgumentException("Acción no reconocida.");
            }
        } catch (IllegalArgumentException e) {
            sesion.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            log("Error al gestionar la compra", e);
            sesion.setAttribute("flashError", "No se pudo completar la operación: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/interno/compras");
    }

    private int registrar(HttpServletRequest request) throws SQLException {
        Integer idProveedor = entero(request.getParameter("idProveedor"));
        String[] ids = request.getParameterValues("idProducto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precioUnitario");

        if (idProveedor == null) {
            throw new IllegalArgumentException("Seleccione un proveedor.");
        }
        if (ids == null || cantidades == null || precios == null || ids.length == 0
                || ids.length != cantidades.length || ids.length != precios.length) {
            throw new IllegalArgumentException("Agregue al menos un producto.");
        }

        List<DetalleCompra> detalles = new ArrayList<>();
        try {
            for (int i = 0; i < ids.length; i++) {
                DetalleCompra d = new DetalleCompra();
                d.setIdProducto(Integer.parseInt(ids[i].trim()));
                d.setCantidad(Integer.parseInt(cantidades[i].trim()));
                d.setPrecioUnitario(new BigDecimal(precios[i].trim()));
                detalles.add(d);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Revise las cantidades y los precios.");
        }

        Compra compra = new Compra();
        compra.setIdProveedor(idProveedor);
        return compraDAO.registrar(compra, detalles);
    }

    private Integer entero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            int n = Integer.parseInt(valor.trim());
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
