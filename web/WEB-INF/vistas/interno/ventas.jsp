<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="entidad.Producto"%>
<%@page import="entidad.Venta"%>
<%@page import="java.math.BigDecimal"%>

<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    List<Venta> ventas = (List<Venta>) request.getAttribute("ventas");

    String csrf = (String) request.getAttribute("csrf");

    String mensaje = (String) session.getAttribute("mensajeVenta");
    String error = (String) session.getAttribute("errorVenta");

    session.removeAttribute("mensajeVenta");
    session.removeAttribute("errorVenta");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ventas | ElectroSpeed</title>
    <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    <link href="${pageContext.request.contextPath}/css/acceso.css"
      rel="stylesheet">
    <style>
        /* Solo estilos específicos del formulario dinámico de ventas. */
        .venta-fila { display: flex; gap: 12px; align-items: end; flex-wrap: wrap; margin-bottom: 12px; }
        .venta-campo { flex: 1 1 240px; min-width: 180px; }
        .venta-fila .btn-eliminar-producto { flex: 0 0 auto; }
        .venta-total { text-align: right; font-weight: 700; font-size: 1.15rem; }
    </style>
</head>

<body>
<jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
    <jsp:param name="activa" value="ventas"/>
</jsp:include>
<main>
    <div class="encabezado-pagina">
        <div class="container-fluid px-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
            <div>
                <h1>Módulo de Ventas</h1>
                <p>Registro de ventas y consulta del historial.</p>
            </div>
            <a href="#formVenta" class="btn btn-es">
                <i class="bi bi-plus-circle me-1"></i> Nueva venta
            </a>
        </div>
    </div>
    <div class="container-fluid px-4 pb-5">

    <% if (mensaje != null) { %>
        <div class="alert alert-success"><%= mensaje %></div>
    <% } %>

    <% if (error != null) { %>
        <div class="alert alert-danger">No se pudo registrar la venta. Revisa los productos y el stock disponible.</div>
    <% } %>

    <div class="panel-es p-4 mb-4">
        <h2 class="h5 mb-3">Registrar nueva venta</h2>

        <form action="<%= request.getContextPath() %>/interno/ventas"
              method="post" id="formVenta">

            <input type="hidden" name="csrf"
                   value="<%= csrf == null ? "" : csrf %>">

            <div class="venta-fila">
                <div class="venta-campo">
                    <label class="form-label" for="tipoComprobante">Tipo de comprobante</label>
                    <select class="form-select" name="tipoComprobante" id="tipoComprobante" required>
                        <option value="BOLETA">Boleta</option>
                        <option value="FACTURA">Factura</option>
                    </select>
                </div>
            </div>

            <h3 class="h6 mt-3 mb-3">Productos</h3>

            <div id="filasProductos"></div>

            <button type="button" class="btn btn-es"
                onclick="agregarProducto()">
                <i class="bi bi-plus-circle me-1"></i>
                Agregar producto
            </button>

            <div class="venta-total my-3">
                Total: S/ <span id="totalVenta">0.00</span>
            </div>

            <button type="submit" id="btnRegistrar" class="btn btn-es">
                <i class="bi bi-save me-1"></i>
                Registrar venta
            </button>
        </form>
    </div>
    <%
    Venta ventaSeleccionada =
        (Venta) request.getAttribute("ventaSeleccionada");

    List<entidad.DetalleVenta> detallesVenta =
        (List<entidad.DetalleVenta>) request.getAttribute("detallesVenta");
%>

<% if (ventaSeleccionada != null) { %>

<div class="panel-es p-4 mb-4" id="detalleVenta">

    <h2 class="h5 mb-3">Detalle de venta N.º <%= ventaSeleccionada.getIdVenta() %></h2>

    <p>
        <strong>Fecha:</strong>
        <%= ventaSeleccionada.getFecha() %>
    </p>

    <p>
        <strong>Comprobante:</strong>
        <%= ventaSeleccionada.getTipoComprobante() %>
    </p>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 tabla-es">
            <thead>
                <tr>
                    <th>Producto</th>
                    <th>Cantidad</th>
                    <th>Precio unitario</th>
                    <th>Subtotal</th>
                </tr>
            </thead>

            <tbody>
                <% if (detallesVenta != null) {
                    for (entidad.DetalleVenta d : detallesVenta) { %>

                <tr>
                    <td><%= d.getNombreProducto() %></td>
                    <td><%= d.getCantidad() %></td>
                    <td>S/ <%= d.getPrecioUnitario() %></td>
                    <td>S/ <%= d.getSubtotal() %></td>
                </tr>

                <%  }
                   } %>
            </tbody>
        </table>
    </div>

    <div class="venta-total my-3">
        Total: S/ <%= ventaSeleccionada.getTotal() %>
    </div>

    <a href="<%= request.getContextPath() %>/interno/ventas">
        Volver al historial
    </a>

</div>

<% } %>               
    <div class="panel-es p-0 mb-4">
        <h2 class="h5 px-4 pt-4 mb-3">Historial de ventas</h2>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0 tabla-es">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Fecha</th>
                        <th>Vendedor (ID)</th>
                        <th>Comprobante</th>
                        <th>Total</th>
                        <th>Acciones</th>
                    </tr>
                </thead>

                <tbody>
                    <% if (ventas != null && !ventas.isEmpty()) {
                        for (Venta v : ventas) { %>
                            <tr>
                                <td><%= v.getIdVenta() %></td>
                                <td><%= v.getFecha() %></td>
                                <td><%= v.getIdUsuario() %></td>
                                <td><%= v.getTipoComprobante() %></td>
                                <td>S/ <%= v.getTotal() %></td>
                                <td>
                                    <a class="btn btn-sm btn-es-outline"
                                        href="<%= request.getContextPath() %>/interno/ventas?idVenta=<%= v.getIdVenta() %>">
                                         <i class="bi bi-eye me-1"></i>
                                         Ver detalle
                                     </a>
                                </td>
                            </tr>
                    <%  }
                       } else { %>
                        <tr>
                            <td colspan="6">
                                Todavía no hay ventas registradas.
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    </div>
</main>

<script>
    const productos = [
        <% if (productos != null) {
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
        %>
        {
            id: <%= p.getIdProducto() %>,
            nombre: "<%= p.getCodigoSku() == null ? "" : p.getCodigoSku().replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n").replace("<", "\\u003c") %> - <%= p.getNombre() == null ? "" : p.getNombre().replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n").replace("<", "\\u003c") %>",
            precio: <%= p.getPrecioVenta() %>,
            stock: <%= p.getStockDisponible() %>
        }<%= i < productos.size() - 1 ? "," : "" %>
        <%  }
           } %>
    ];

    function agregarProducto() {
        if (productos.length === 0) {
            alert("No hay productos con stock disponible.");
            return;
        }

        const contenedor = document.getElementById("filasProductos");

        const fila = document.createElement("div");
        fila.className = "venta-fila";

        const campoProducto = document.createElement("div");
        campoProducto.className = "venta-campo";

        const etiquetaProducto = document.createElement("label");
        etiquetaProducto.textContent = "Producto";
        etiquetaProducto.className = "form-label";

        const selector = document.createElement("select");
        selector.name = "idProducto";
        selector.className = "form-select";
        selector.required = true;

        productos.forEach(p => {
            const opcion = document.createElement("option");
            opcion.value = p.id;
            opcion.textContent = p.nombre
                    + " | S/ " + p.precio.toFixed(2)
                    + " | Stock: " + p.stock;
            selector.appendChild(opcion);
        });

        campoProducto.appendChild(etiquetaProducto);
        campoProducto.appendChild(selector);

        const campoCantidad = document.createElement("div");
        campoCantidad.className = "venta-campo";

        const etiquetaCantidad = document.createElement("label");
        etiquetaCantidad.textContent = "Cantidad";
        etiquetaCantidad.className = "form-label";

        const cantidad = document.createElement("input");
        cantidad.type = "number";
        cantidad.name = "cantidad";
        cantidad.className = "form-control";
        cantidad.min = "1";
        cantidad.value = "1";
        cantidad.required = true;

        campoCantidad.appendChild(etiquetaCantidad);
        campoCantidad.appendChild(cantidad);

        const botonEliminar = document.createElement("button");
        botonEliminar.type = "button";
        botonEliminar.className = "btn btn-danger btn-eliminar-producto";
        botonEliminar.textContent = "Eliminar";

        botonEliminar.onclick = function () {
            fila.remove();
            calcularTotal();
        };

        selector.onchange = calcularTotal;
        cantidad.oninput = calcularTotal;

        fila.appendChild(campoProducto);
        fila.appendChild(campoCantidad);
        fila.appendChild(botonEliminar);

        contenedor.appendChild(fila);

        calcularTotal();
    }

    function calcularTotal() {
        let total = 0;

        const filas = document.querySelectorAll("#filasProductos .venta-fila");

        filas.forEach(fila => {
            const id = Number(fila.querySelector("select").value);
            const cantidad = Number(fila.querySelector("input").value);

            const producto = productos.find(p => p.id === id);

            if (producto && cantidad > 0) {
                total += producto.precio * cantidad;
            }
        });

        document.getElementById("totalVenta").textContent =
                total.toFixed(2);
    }

    document.getElementById("formVenta").addEventListener("submit", function (e) {
        const filas = document.querySelectorAll("#filasProductos .venta-fila");

        if (filas.length === 0) {
            e.preventDefault();
            alert("Debes agregar al menos un producto.");
            return;
        }

        const seleccionados = new Set();

        for (const fila of filas) {
            const id = Number(fila.querySelector("select").value);
            const cantidad = Number(fila.querySelector("input").value);
            const producto = productos.find(p => p.id === id);

            if (!producto || !Number.isInteger(cantidad)
                    || cantidad <= 0 || cantidad > producto.stock) {
                e.preventDefault();
                alert("Verifica las cantidades y el stock disponible.");
                return;
            }

            if (seleccionados.has(id)) {
                e.preventDefault();
                alert("No agregues el mismo producto dos veces.");
                return;
            }

            seleccionados.add(id);
        }

        document.getElementById("btnRegistrar").disabled = true;
    });

    agregarProducto();
</script>
<jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>
</body>
</html>
