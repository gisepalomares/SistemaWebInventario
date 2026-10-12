/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ProductoDAO;
import entidad.Carrito;
import entidad.ItemCarrito;
import entidad.Producto;
import entidad.Usuario;
import interfaces.IProductoDAO;
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
 * Carrito de compras (zona pública, solo clientes con sesión).
 * GET  /carrito                         -> muestra el carrito (re-validado contra el stock real)
 * POST /carrito  accion=agregar|actualizar|quitar|vaciar
 *
 * El carrito NO reserva stock: solo valida contra el disponible para no mostrar
 * cantidades imposibles. La reserva real se hace al confirmar el pedido (paso 2).
 */
@WebServlet(name = "CarritoServlet", urlPatterns = {"/carrito"})
public class CarritoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/publico/carrito.jsp";

    private final IProductoDAO productoDAO = new ProductoDAO();

    // ------------------------------------------------------------------
    //  GET: ver el carrito
    // ------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (clienteEnSesion(request, response) == null) {
            return;
        }
        HttpSession sesion = request.getSession();
        Carrito carrito = obtenerCarrito(sesion);

        List<String> avisos = new ArrayList<>();
        try {
            sincronizar(carrito, avisos);
        } catch (SQLException e) {
            log("Error al sincronizar el carrito con el stock", e);
            avisos.add("No se pudo verificar el stock actual. Intente nuevamente en unos minutos.");
        }

        request.setAttribute("avisos", avisos);
        moverMensaje(sesion, request, "flashOk");
        moverMensaje(sesion, request, "flashError");
        request.setAttribute("csrf", Csrf.token(sesion));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // ------------------------------------------------------------------
    //  POST: agregar / actualizar / quitar / vaciar
    // ------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (clienteEnSesion(request, response) == null) {
            return;
        }
        if (!Csrf.valido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solicitud no válida. Recargue la página e intente de nuevo.");
            return;
        }

        HttpSession sesion = request.getSession();
        Carrito carrito = obtenerCarrito(sesion);
        String accion = request.getParameter("accion") == null ? "" : request.getParameter("accion");
        String destino = "/carrito";

        try {
            switch (accion) {
                case "agregar":
                    agregar(request, sesion, carrito);
                    destino = destinoVolver(request);   // vuelve al catálogo con sus filtros
                    break;
                case "actualizar":
                    actualizar(request, sesion, carrito);
                    break;
                case "quitar":
                    Integer idQuitar = entero(request.getParameter("idProducto"));
                    if (idQuitar != null) {
                        carrito.quitar(idQuitar);
                        sesion.setAttribute("flashOk", "Producto quitado del carrito.");
                    }
                    break;
                case "vaciar":
                    carrito.vaciar();
                    sesion.setAttribute("flashOk", "El carrito quedó vacío.");
                    break;
                default:
                    sesion.setAttribute("flashError", "Acción no reconocida.");
            }
        } catch (SQLException e) {
            log("Error en el carrito", e);
            sesion.setAttribute("flashError", "No se pudo consultar el stock. Intente nuevamente.");
        }

        response.sendRedirect(request.getContextPath() + destino);
    }

    // ------------------------------------------------------------------
    //  Acciones
    // ------------------------------------------------------------------
    private void agregar(HttpServletRequest request, HttpSession sesion, Carrito carrito)
            throws SQLException {

        Integer id = entero(request.getParameter("idProducto"));
        Integer cantidad = entero(request.getParameter("cantidad"));
        if (cantidad == null) {
            cantidad = 1;
        }
        if (id == null || cantidad < 1) {
            sesion.setAttribute("flashError", "Datos no válidos.");
            return;
        }

        // Siempre se consulta la BD: no se confía en el precio ni en el stock que envía el navegador
        Producto p = productoDAO.buscarPorId(id);
        if (p == null) {
            sesion.setAttribute("flashError", "El producto ya no está disponible.");
            return;
        }

        int disponible = p.getStockDisponible();
        ItemCarrito existente = carrito.obtener(id);
        int enCarrito = (existente == null) ? 0 : existente.getCantidad();

        if (disponible <= 0) {
            sesion.setAttribute("flashError", p.getNombre() + " está agotado.");
            return;
        }
        if (enCarrito >= disponible) {
            sesion.setAttribute("flashError", "Ya tiene en su carrito todo el stock disponible de "
                    + p.getNombre() + " (" + disponible + ").");
            return;
        }

        int nueva = enCarrito + cantidad;
        if (nueva > disponible) {
            nueva = disponible;
            sesion.setAttribute("flashError", "Solo hay " + disponible + " unidad(es) disponible(s) de "
                    + p.getNombre() + ". Se agregó el máximo posible.");
        } else {
            sesion.setAttribute("flashOk", p.getNombre() + " se agregó al carrito.");
        }
        carrito.poner(aItem(p, nueva));
    }

    private void actualizar(HttpServletRequest request, HttpSession sesion, Carrito carrito)
            throws SQLException {

        Integer id = entero(request.getParameter("idProducto"));
        Integer cantidad = entero(request.getParameter("cantidad"));
        if (id == null || cantidad == null || carrito.obtener(id) == null) {
            sesion.setAttribute("flashError", "Datos no válidos.");
            return;
        }
        if (cantidad < 1) {
            carrito.quitar(id);
            sesion.setAttribute("flashOk", "Producto quitado del carrito.");
            return;
        }

        Producto p = productoDAO.buscarPorId(id);
        if (p == null || p.getStockDisponible() <= 0) {
            carrito.quitar(id);
            sesion.setAttribute("flashError", "El producto ya no está disponible y se quitó del carrito.");
            return;
        }
        if (cantidad > p.getStockDisponible()) {
            cantidad = p.getStockDisponible();
            sesion.setAttribute("flashError", "Solo hay " + cantidad + " unidad(es) disponible(s) de "
                    + p.getNombre() + ". Se ajustó la cantidad.");
        } else {
            sesion.setAttribute("flashOk", "Cantidad actualizada.");
        }
        carrito.poner(aItem(p, cantidad));
    }

    /**
     * Pone el carrito al día con la BD: precios, stock y productos que ya no se pueden vender.
     * Las novedades se devuelven en "avisos" para mostrárselas al cliente.
     */
    private void sincronizar(Carrito carrito, List<String> avisos) throws SQLException {
        for (ItemCarrito item : new ArrayList<>(carrito.getItems())) {
            Producto p = productoDAO.buscarPorId(item.getIdProducto());

            if (p == null || p.getStockDisponible() <= 0) {
                carrito.quitar(item.getIdProducto());
                avisos.add(item.getNombre() + " ya no está disponible y se quitó del carrito.");
                continue;
            }

            int cantidad = item.getCantidad();
            if (cantidad > p.getStockDisponible()) {
                cantidad = p.getStockDisponible();
                avisos.add("El stock de " + p.getNombre() + " cambió: se ajustó la cantidad a " + cantidad + ".");
            }
            if (item.getPrecioUnitario().compareTo(p.getPrecioVenta()) != 0) {
                avisos.add("El precio de " + p.getNombre() + " cambió a S/ " + p.getPrecioVenta() + ".");
            }
            carrito.poner(aItem(p, cantidad));
        }
    }

    // ------------------------------------------------------------------
    //  Auxiliares
    // ------------------------------------------------------------------
    private ItemCarrito aItem(Producto p, int cantidad) {
        ItemCarrito i = new ItemCarrito();
        i.setIdProducto(p.getIdProducto());
        i.setCodigoSku(p.getCodigoSku());
        i.setNombre(p.getNombre());
        i.setPrecioUnitario(p.getPrecioVenta());
        i.setCantidad(cantidad);
        i.setStockDisponible(p.getStockDisponible());
        return i;
    }

    private Carrito obtenerCarrito(HttpSession sesion) {
        Carrito c = (Carrito) sesion.getAttribute("carrito");
        if (c == null) {
            c = new Carrito();
            sesion.setAttribute("carrito", c);
        }
        return c;
    }

    /** Solo los clientes con sesión usan el carrito. Si no, redirige y devuelve null. */
    private Usuario clienteEnSesion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession sesion = request.getSession(false);
        Usuario u = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");

        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (u.isPersonal()) {
            sesion.setAttribute("flashError",
                    "El personal no realiza pedidos en línea. Use una cuenta de cliente.");
            response.sendRedirect(request.getContextPath() + "/catalogo");
            return null;
        }
        return u;
    }

    /** Vuelve al catálogo conservando sus filtros (solo se aceptan caracteres seguros de una query). */
    private String destinoVolver(HttpServletRequest request) {
        String v = request.getParameter("volver");
        if (v != null && !v.isEmpty() && v.matches("[\\w=&%+.\\-]{1,200}")) {
            return "/catalogo?" + v;
        }
        return "/catalogo";
    }

    private void moverMensaje(HttpSession sesion, HttpServletRequest request, String clave) {
        Object m = sesion.getAttribute(clave);
        if (m != null) {
            request.setAttribute(clave, m);
            sesion.removeAttribute(clave);
        }
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
}
