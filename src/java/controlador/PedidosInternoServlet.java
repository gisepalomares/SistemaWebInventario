/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import entidad.Usuario;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Bandeja de pedidos en línea para el personal (administrador y vendedor). URL:
 * /interno/pedidos?estado=PAGADO&q=texto
 *
 * Acciones (POST, campo "accion"): pagar | listo | entregar | cancelar El
 * acceso del personal lo garantiza AutenticacionFiltro (/interno/*).
 */
@WebServlet(name = "PedidosInternoServlet", urlPatterns = {"/interno/pedidos"})
public class PedidosInternoServlet extends HttpServlet {

    private static final List<String> ESTADOS = Arrays.asList(
            "PENDIENTE_PAGO", "PAGADO", "LISTO_RECOGER", "ENTREGADO", "VENCIDO", "CANCELADO");

    private final PedidoFacade facade = new PedidoFacade();

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

        // Antes de mostrar la bandeja, se vencen los pedidos cuyo plazo ya terminó
        try {
            facade.vencerPedidos();
        } catch (SQLException e) {
            log("Error al vencer pedidos", e);
        }

        String estado = estadoValido(request.getParameter("estado"));
        String texto = limpiar(request.getParameter("q"));

        try {
            request.setAttribute("pedidos", facade.listarGestion(estado, texto));

            // Contadores de las pestañas (todos los estados, aunque no haya pedidos)
            Map<String, Integer> brutos = facade.contarPorEstado();
            Map<String, Integer> conteos = new LinkedHashMap<>();
            int total = 0;
            for (String e : ESTADOS) {
                Integer n = brutos.get(e);
                conteos.put(e, n == null ? 0 : n);
                total += (n == null ? 0 : n);
            }
            conteos.put("TODOS", total);
            request.setAttribute("conteos", conteos);
        } catch (SQLException e) {
            log("Error al listar pedidos", e);
            request.setAttribute("error", "No se pudo cargar la lista de pedidos.");
        }

        request.setAttribute("estadoSel", estado);
        request.setAttribute("q", texto);
        request.setAttribute("csrf", Csrf.token(sesion));
        request.getRequestDispatcher("/WEB-INF/vistas/interno/pedidos.jsp").forward(request, response);
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
        if (yo == null || !yo.isPersonal()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el personal puede gestionar pedidos.");
            return;
        }

        String accion = limpiar(request.getParameter("accion"));
        Integer idPedido = entero(request.getParameter("idPedido"));

        try {
            if (idPedido == null) {
                throw new PedidoException("Pedido no válido.");
            }
            switch (accion) {
                case "pagar":
                    facade.registrarPagoEnTienda(idPedido, request.getParameter("medio"),
                            request.getParameter("referencia"));
                    sesion.setAttribute("flashOk", "Pago en "
                            + ("EFECTIVO".equals(request.getParameter("medio")) ? "efectivo" : "POS")
                            + " registrado. El pedido pasó a «Pagado».");
                    break;
                case "listo":
                    facade.marcarListo(idPedido);
                    sesion.setAttribute("flashOk", "Pedido marcado como listo para recoger.");
                    break;
                case "entregar": {
                    int idVenta = facade.entregar(idPedido, yo, request.getParameter("codigo"));
                    sesion.setAttribute("flashOk", "Pedido entregado. Se registró la venta N.° " + idVenta
                            + " y se descontó el stock.");
                    break;
                }
                case "cancelar":
                    facade.cancelar(idPedido);
                    sesion.setAttribute("flashOk", "Pedido cancelado. Se liberó el stock reservado.");
                    break;
                default:
                    throw new PedidoException("Acción no reconocida.");
            }
        } catch (PedidoException e) {
            sesion.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            log("Error al gestionar el pedido", e);
            sesion.setAttribute("flashError", "No se pudo completar la operación. Intente nuevamente.");
        }

        response.sendRedirect(request.getContextPath() + "/interno/pedidos" + filtrosParaUrl(request));
    }

    /**
     * Conserva la pestaña y la búsqueda después de una acción.
     */
    private String filtrosParaUrl(HttpServletRequest request) throws IOException {
        String estado = estadoValido(request.getParameter("estado"));
        String texto = limpiar(request.getParameter("q"));
        StringBuilder sb = new StringBuilder();
        if (!estado.isEmpty()) {
            sb.append("estado=").append(estado);
        }
        if (!texto.isEmpty()) {
            sb.append(sb.length() == 0 ? "" : "&").append("q=").append(URLEncoder.encode(texto, "UTF-8"));
        }
        return sb.length() == 0 ? "" : "?" + sb;
    }

    /**
     * Solo se aceptan los estados conocidos; cualquier otro valor significa
     * "todos".
     */
    private String estadoValido(String estado) {
        return (estado != null && ESTADOS.contains(estado)) ? estado : "";
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

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
