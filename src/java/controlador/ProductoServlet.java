import dao.CategoriaDAO;
import dao.ProductoDAO;
import dao.ProveedorDAO;
import entidad.Producto;
import entidad.Usuario;
import interfaces.ICategoriaDAO;
import interfaces.IProductoDAO;
import interfaces.IProveedorDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.Csrf;

/**
 * Gestión de productos (zona interna). URL: /interno/productos
 *
 *  - GET  : lo ven el ADMINISTRADOR y el VENDEDOR (consulta de inventario).
 *  - POST : SOLO el ADMINISTRADOR (crear | editar | estado). El filtro protege por ruta y
 *           esa ruta la comparten los dos roles, por eso el permiso se valida aquí.
 *
 * Mismo patrón que UsuarioServlet: Post-Redirect-Get con mensajes "flash" en la sesión.
 * Novedad: si el formulario tiene un error, se vuelve a abrir con lo que el usuario escribió.
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {"/interno/productos"})
public class ProductoServlet extends HttpServlet {

    private static final Pattern SKU = Pattern.compile("^[A-Z0-9][A-Z0-9._-]{1,19}$");
    private static final Pattern PRECIO = Pattern.compile("^\\d{1,8}(\\.\\d{1,2})?$");
    private static final String[] CAMPOS_FORMULARIO
            = {"id", "sku", "nombre", "descripcion", "precio", "categoria", "proveedor", "minimo", "stock"};

    private final IProductoDAO productoDAO = new ProductoDAO();
    private final ICategoriaDAO categoriaDAO = new CategoriaDAO();
    private final IProveedorDAO proveedorDAO = new ProveedorDAO();

    // =====================================================================
    //  GET: lista con filtros
    // =====================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession sesion = request.getSession();
        Usuario yo = (Usuario) sesion.getAttribute("usuario");
        boolean admin = yo != null && yo.isAdministrador();

        recuperarMensajes(sesion, request);

        String texto = limpiar(request.getParameter("q"));
        Integer idCategoria = entero(request.getParameter("categoria"));
        // El vendedor solo ve productos activos (los inactivos no se venden)
        String estado = admin
                ? permitido(request.getParameter("estado"), "activos", "activos", "inactivos", "todos")
                : "activos";
        String stock = permitido(request.getParameter("stock"), "", "", "bajo", "agotado");

        try {
            List<Producto> base = productoDAO.listarGestion(texto, idCategoria, estado);

            // Los contadores de alerta se calculan con la lista ANTES del filtro de stock
            int totalBajo = 0;
            int totalAgotado = 0;
            List<Producto> visibles = new ArrayList<>();
            for (Producto p : base) {
                boolean agotado = p.isEstado() && p.isAgotado();
                boolean bajo = p.isEstado() && p.isStockBajo();
                if (agotado) {
                    totalAgotado++;
                } else if (bajo) {
                    totalBajo++;
                }
                if (("bajo".equals(stock) && !bajo) || ("agotado".equals(stock) && !agotado)) {
                    continue;
                }
                visibles.add(p);
            }
            request.setAttribute("productos", visibles);
            request.setAttribute("totalListado", base.size());
            request.setAttribute("totalBajo", totalBajo);
            request.setAttribute("totalAgotado", totalAgotado);
            request.setAttribute("categorias", categoriaDAO.listar());
            if (admin) {
                request.setAttribute("proveedores", proveedorDAO.listar());
            }
        } catch (SQLException e) {
            log("Error al listar productos", e);
            request.setAttribute("error", "No se pudo cargar la lista de productos.");
        }

        request.setAttribute("q", texto);
        request.setAttribute("categoriaSel", idCategoria);
        request.setAttribute("estadoSel", estado);
        request.setAttribute("stockSel", stock);
        request.setAttribute("csrf", Csrf.token(sesion));
        request.getRequestDispatcher("/WEB-INF/vistas/interno/productos.jsp").forward(request, response);
    }

    // =====================================================================
    //  POST: crear | editar | estado   (solo administrador)
    // =====================================================================
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
        if (yo == null || !yo.isAdministrador()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el administrador puede modificar productos.");
            return;
        }

        String accion = limpiar(request.getParameter("accion"));
        boolean activar = "1".equals(request.getParameter("activo"));
        String mensajeOk = null;
        String mensajeError;

        try {
            switch (accion) {
                case "crear":
                    mensajeError = crear(request);
                    mensajeOk = "Producto registrado correctamente.";
                    break;
                case "editar":
                    mensajeError = editar(request);
                    mensajeOk = "Producto actualizado correctamente.";
                    break;
                case "estado":
                    mensajeError = cambiarEstado(request, activar);
                    mensajeOk = activar
                            ? "Producto activado. Vuelve a mostrarse en el catálogo."
                            : "Producto desactivado. Ya no se muestra en el catálogo.";
                    break;
                default:
                    mensajeError = "Acción no reconocida.";
            }
        } catch (SQLException e) {
            log("Error en gestión de productos (" + accion + ")", e);
            // 1062 = valor duplicado en una columna única (el código SKU)
            mensajeError = (e.getErrorCode() == 1062)
                    ? "Ya existe un producto con ese código (SKU)."
                    : "Ocurrió un error con la base de datos. Intente nuevamente.";
        }

        if (mensajeError != null) {
            sesion.setAttribute("flashError", mensajeError);
            if ("crear".equals(accion) || "editar".equals(accion)) {
                guardarFormulario(sesion, request, accion);
            }
        } else {
            sesion.setAttribute("flashOk", mensajeOk);
        }
        response.sendRedirect(request.getContextPath() + "/interno/productos");
    }

    // Cada método devuelve null si todo salió bien, o el mensaje de error.

    private String crear(HttpServletRequest request) throws SQLException {
        Producto p = new Producto();
        String error = leerYValidar(request, p, true);
        if (error != null) {
            return error;
        }
        return productoDAO.registrar(p) ? null : "No se pudo registrar el producto.";
    }

    private String editar(HttpServletRequest request) throws SQLException {
        Integer id = entero(request.getParameter("id"));
        Producto actual = (id == null) ? null : productoDAO.obtenerParaGestion(id);
        if (actual == null) {
            return "El producto no existe.";
        }
        Producto p = new Producto();
        p.setIdProducto(actual.getIdProducto());
        String error = leerYValidar(request, p, false);
        if (error != null) {
            return error;
        }
        return productoDAO.actualizar(p) ? null : "No se pudo actualizar el producto.";
    }

    private String cambiarEstado(HttpServletRequest request, boolean activar) throws SQLException {
        Integer id = entero(request.getParameter("id"));
        Producto p = (id == null) ? null : productoDAO.obtenerParaGestion(id);
        if (p == null) {
            return "El producto no existe.";
        }
        // Regla: no se desactiva un producto cuyo stock está separado para pedidos en línea
        if (!activar && p.getStockReservado() > 0) {
            return "No se puede desactivar: hay " + p.getStockReservado()
                    + " unidad(es) reservada(s) por pedidos en línea pendientes.";
        }
        return productoDAO.cambiarEstado(p.getIdProducto(), activar) ? null : "No se pudo cambiar el estado.";
    }

    /**
     * Lee el formulario, valida y llena el producto. Devuelve null si todo es correcto.
     * El stock inicial solo se lee al CREAR; al editar el stock no se toca.
     */
    private String leerYValidar(HttpServletRequest request, Producto p, boolean esNuevo) throws SQLException {
        String sku = limpiar(request.getParameter("sku")).toUpperCase(Locale.ROOT);
        String nombre = limpiar(request.getParameter("nombre"));
        String descripcion = limpiar(request.getParameter("descripcion"));
        BigDecimal precio = precio(request.getParameter("precio"));
        Integer idCategoria = entero(request.getParameter("categoria"));
        String textoProveedor = limpiar(request.getParameter("proveedor"));
        Integer idProveedor = textoProveedor.isEmpty() ? null : entero(textoProveedor);
        Integer minimo = entero(request.getParameter("minimo"));

        if (!SKU.matcher(sku).matches()) {
            return "El código (SKU) debe tener de 2 a 20 caracteres: letras, números, guion, punto o guion bajo.";
        }
        if (nombre.length() < 3 || nombre.length() > 120) {
            return "Ingrese el nombre del producto (de 3 a 120 caracteres).";
        }
        if (descripcion.length() > 255) {
            return "La descripción no puede superar los 255 caracteres.";
        }
        if (precio == null) {
            return "Ingrese un precio válido (hasta 2 decimales).";
        }
        if (precio.signum() <= 0) {
            return "El precio debe ser mayor a 0.";
        }
        if (idCategoria == null) {
            return "Seleccione una categoría.";
        }
        if (!textoProveedor.isEmpty() && idProveedor == null) {
            return "El proveedor seleccionado no es válido.";
        }
        if (minimo == null || minimo < 0 || minimo > 99999) {
            return "El stock mínimo debe ser un número entre 0 y 99 999.";
        }
        if (esNuevo) {
            Integer stock = entero(request.getParameter("stock"));
            if (stock == null || stock < 0 || stock > 999999) {
                return "El stock inicial debe ser un número entre 0 y 999 999.";
            }
            p.setStockActual(stock);
        }

        // Verificaciones contra la base de datos
        if (categoriaDAO.buscarPorId(idCategoria) == null) {
            return "La categoría seleccionada no existe.";
        }
        if (idProveedor != null && proveedorDAO.buscarPorId(idProveedor) == null) {
            return "El proveedor seleccionado no existe.";
        }
        Producto conEseSku = productoDAO.buscarPorSku(sku);
        if (conEseSku != null && conEseSku.getIdProducto() != p.getIdProducto()) {
            return "Ya existe un producto con el código " + sku + ".";
        }

        p.setCodigoSku(sku);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioVenta(precio);
        p.setIdCategoria(idCategoria);
        p.setIdProveedor(idProveedor);
        p.setStockMinimo(minimo);
        return null;
    }

    // =====================================================================
    //  Mensajes flash y formulario previo
    // =====================================================================
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

    // =====================================================================
    //  Auxiliares de lectura
    // =====================================================================
    private BigDecimal precio(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim().replace(',', '.');
        return PRECIO.matcher(t).matches() ? new BigDecimal(t).setScale(2) : null;
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

    /** Devuelve el valor si está entre los permitidos; si no, el valor por defecto. */
    private String permitido(String valor, String porDefecto, String... permitidos) {
        if (valor != null) {
            for (String p : permitidos) {
                if (p.equals(valor)) {
                    return valor;
                }
            }
        }
        return porDefecto;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}