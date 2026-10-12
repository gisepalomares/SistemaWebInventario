/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import dao.ProveedorDAO;
import entidad.Proveedor;
import interfaces.IProveedorDAO;
import java.io.IOException;
import java.sql.SQLException;
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
@WebServlet(name = "ProveedorServlet",
        urlPatterns = {"/interno/proveedores"})
public class ProveedorServlet extends HttpServlet {

    private final IProveedorDAO proveedorDAO = new ProveedorDAO();

    private static final String VISTA =
            "/WEB-INF/vistas/interno/proveedores.jsp";

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession sesion = request.getSession();

        for (String clave : new String[]{"flashOk", "flashError"}) {
            Object mensaje = sesion.getAttribute(clave);

            if (mensaje != null) {
                request.setAttribute(
                        clave.equals("flashOk") ? "ok" : "error",
                        mensaje
                );
                sesion.removeAttribute(clave);
            }
        }

        request.setAttribute("csrf", Csrf.token(sesion));
        String accion = request.getParameter("accion");

        try {
            if ("editar".equals(accion)) {

                int id = Integer.parseInt(
                        request.getParameter("id"));

                Proveedor proveedor =
                        proveedorDAO.buscarPorId(id);

                if (proveedor == null) {
                    response.sendError(404,
                            "Proveedor no encontrado");
                    return;
                }

                request.setAttribute("proveedor", proveedor);
            }

            String buscar = request.getParameter("buscar");

            List<Proveedor> proveedores;

            if (buscar != null && !buscar.trim().isEmpty()) {
                proveedores = proveedorDAO.buscar(buscar.trim());
            } else {
                proveedores = proveedorDAO.listar();
            }

            request.setAttribute("proveedores", proveedores);

            request.getRequestDispatcher(VISTA)
                    .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(400, "ID de proveedor inválido");

        } catch (SQLException e) {
            log("Error al consultar proveedores", e);
            throw new ServletException(
                    "No se pudieron consultar los proveedores", e);
        }
    }

    
@Override
protected void doPost(HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");

    if (!Csrf.valido(request)) {
        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Solicitud no válida. Recargue la página."
        );
        return;
    }

    HttpSession sesion = request.getSession();

    String accion = valor(request, "accion");
    String mensajeError = null;
    String mensajeOk = null;

    try {

        if (!"registrar".equals(accion)
                && !"actualizar".equals(accion)
                && !"eliminar".equals(accion)) {

            mensajeError = "Acción no válida.";
        } else if ("eliminar".equals(accion)) {

        int id = Integer.parseInt(
                valor(request, "idProveedor"));

        if (id <= 0) {
            mensajeError = "ID de proveedor inválido.";
        } else {

            boolean resultado = proveedorDAO.eliminar(id);

            if (resultado) {
                mensajeOk = "Proveedor eliminado correctamente.";
            } else {
                mensajeError = "No se encontró el proveedor para eliminar.";
            }
        }

        } else {

            Proveedor p = new Proveedor();

            p.setRuc(valor(request, "ruc"));
            p.setRazonSocial(valor(request, "razonSocial"));
            p.setNombreComercial(valor(request, "nombreComercial"));
            p.setDireccion(valor(request, "direccion"));
            p.setPersonaContacto(valor(request, "personaContacto"));
            p.setTelefono(valor(request, "telefono"));
            p.setCorreo(valor(request, "correo"));

            if (!p.getRuc().matches("\\d{11}")) {

                mensajeError =
                        "El RUC debe contener exactamente 11 dígitos.";

            } else if (p.getRazonSocial().isEmpty()) {

                mensajeError =
                        "Ingrese la razón social del proveedor.";

            } else if (p.getRazonSocial().length() > 120
                    || p.getNombreComercial().length() > 120
                    || p.getDireccion().length() > 200
                    || p.getPersonaContacto().length() > 100
                    || p.getTelefono().length() > 20
                    || p.getCorreo().length() > 120) {

                mensajeError =
                        "Uno de los campos supera la longitud permitida.";

            } else if (!p.getCorreo().isEmpty()
                    && !p.getCorreo().matches(
                            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

                mensajeError = "Ingrese un correo válido.";

            } else if ("registrar".equals(accion)) {

                boolean resultado = proveedorDAO.registrar(p);

                if (resultado) {
                    mensajeOk = "Proveedor registrado correctamente.";
                } else {
                    mensajeError = "No se pudo registrar el proveedor.";
                }

            } else {

                int id = Integer.parseInt(
                        valor(request, "idProveedor"));

                if (id <= 0) {

                    mensajeError = "ID de proveedor inválido.";

                } else {

                    p.setIdProveedor(id);

                    boolean resultado = proveedorDAO.actualizar(p);

                    if (resultado) {
                        mensajeOk = "Proveedor actualizado correctamente.";
                    } else {
                        mensajeError =
                                "No se encontró el proveedor para actualizar.";
                    }
                }
            }
        }

    } catch (NumberFormatException e) {

        mensajeError = "ID de proveedor inválido.";

    } catch (SQLException e) {

        log("Error al guardar proveedor", e);

       if (e.getErrorCode() == 1062) {
            mensajeError = "Ya existe un proveedor con ese RUC.";
        } else if (e.getErrorCode() == 1451) {
            mensajeError = "No se puede eliminar el proveedor porque tiene registros relacionados.";
        } else {
            mensajeError = "Ocurrió un error con la base de datos. Intente nuevamente.";
        } 
    }

    if (mensajeError != null) {
        sesion.setAttribute("flashError", mensajeError);
    } else {
        sesion.setAttribute("flashOk", mensajeOk);
    }

    response.sendRedirect(
            request.getContextPath() + "/interno/proveedores");
}

    

    private String valor(HttpServletRequest request,
            String nombre) {

        String dato = request.getParameter(nombre);
        return dato == null ? "" : dato.trim();
    }
}