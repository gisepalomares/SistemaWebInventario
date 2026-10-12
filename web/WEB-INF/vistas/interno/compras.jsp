<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="java.util.List"%>
<%@page import="entidad.Producto"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%!
    /** Texto seguro para insertarlo dentro de un string de JavaScript. */
    private String js(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("<", "\\u003c");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Compras | ElectroSpeed</title>
    <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    <link href="${ctx}/css/acceso.css" rel="stylesheet">
    <style>
        /* Solo estilos específicos del formulario dinámico de compras (igual que en ventas). */
        .compra-fila { display: flex; gap: 12px; align-items: end; flex-wrap: wrap; margin-bottom: 12px; }
        .compra-campo { flex: 1 1 200px; min-width: 160px; }
        .compra-campo-producto { flex: 2 1 280px; }
        .compra-fila .btn-eliminar-producto { flex: 0 0 auto; }
        .compra-total { text-align: right; font-weight: 700; font-size: 1.15rem; }
    </style>
</head>

<body>
<jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
    <jsp:param name="activa" value="compras"/>
</jsp:include>
<main>
    <div class="encabezado-pagina">
        <div class="container-fluid px-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
            <div>
                <h1>Módulo de Compras</h1>
                <p>Entradas de mercadería al almacén desde los proveedores.</p>
            </div>
            <a href="#formCompra" class="btn btn-es">
                <i class="bi bi-plus-circle me-1"></i> Nueva compra
            </a>
        </div>
    </div>

    <div class="container-fluid px-4 pb-5">

        <c:if test="${not empty ok}">
            <div class="alert alert-success"><i class="bi bi-check-circle me-1"></i><c:out value="${ok}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-1"></i><c:out value="${error}"/></div>
        </c:if>

        <%-- ================= Registrar compra ================= --%>
        <div class="panel-es p-4 mb-4">
            <h2 class="h5 mb-3">Registrar nueva compra</h2>

            <form action="${ctx}/interno/compras" method="post" id="formCompra">
                <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                <input type="hidden" name="accion" value="registrar">

                <div class="compra-fila">
                    <div class="compra-campo">
                        <label class="form-label" for="idProveedor">Proveedor</label>
                        <select class="form-select" name="idProveedor" id="idProveedor" required>
                            <option value="">Seleccione un proveedor</option>
                            <c:forEach var="pr" items="${proveedores}">
                                <option value="${pr.idProveedor}">
                                    <c:out value="${pr.razonSocial}"/> (RUC <c:out value="${pr.ruc}"/>)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <h3 class="h6 mt-3 mb-3">Productos</h3>

                <div id="filasProductos"></div>

                <button type="button" class="btn btn-es" onclick="agregarProducto()">
                    <i class="bi bi-plus-circle me-1"></i> Agregar producto
                </button>

                <div class="compra-total my-3">
                    Total: S/ <span id="totalCompra">0.00</span>
                </div>

                <button type="submit" id="btnRegistrar" class="btn btn-es">
                    <i class="bi bi-save me-1"></i> Registrar compra
                </button>
            </form>
        </div>

        <%-- ================= Detalle de una compra ================= --%>
        <c:if test="${not empty compraSeleccionada}">
            <div class="panel-es p-4 mb-4" id="detalleCompra">
                <h2 class="h5 mb-3">Detalle de compra N.º ${compraSeleccionada.idCompra}</h2>

                <p><strong>Fecha:</strong> <c:out value="${compraSeleccionada.fecha}"/></p>
                <p><strong>Proveedor:</strong> <c:out value="${compraSeleccionada.proveedor}"/></p>
                <p><strong>Estado:</strong> <c:out value="${compraSeleccionada.estado}"/></p>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0 tabla-es">
                        <thead>
                            <tr>
                                <th>Producto</th>
                                <th>Cantidad</th>
                                <th>Precio de compra</th>
                                <th>Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="d" items="${detallesCompra}">
                                <tr>
                                    <td><c:out value="${d.nombreProducto}"/></td>
                                    <td>${d.cantidad}</td>
                                    <td>S/ ${d.precioUnitario}</td>
                                    <td>S/ ${d.subtotal}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="compra-total my-3">Total: S/ ${compraSeleccionada.total}</div>

                <a href="${ctx}/interno/compras">Volver al historial</a>
            </div>
        </c:if>

        <%-- ================= Historial ================= --%>
        <div class="panel-es p-0 mb-4">
            <h2 class="h5 px-4 pt-4 mb-3">Historial de compras</h2>

            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 tabla-es">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Fecha</th>
                            <th>Proveedor</th>
                            <th>Total</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="co" items="${compras}">
                            <tr>
                                <td>${co.idCompra}</td>
                                <td><c:out value="${co.fecha}"/></td>
                                <td><c:out value="${co.proveedor}"/></td>
                                <td>S/ ${co.total}</td>
                                <td>
                                    <span class="badge ${co.anulada ? 'text-bg-secondary' : 'text-bg-success'}">
                                        <c:out value="${co.estado}"/>
                                    </span>
                                </td>
                                <td class="d-flex flex-wrap gap-2">
                                    <a class="btn btn-sm btn-es-outline"
                                       href="${ctx}/interno/compras?idCompra=${co.idCompra}#detalleCompra">
                                        <i class="bi bi-eye me-1"></i> Ver detalle
                                    </a>
                                    <c:if test="${not co.anulada}">
                                        <form method="post" action="${ctx}/interno/compras" class="m-0"
                                              onsubmit="return confirm('¿Anular la compra N.º ${co.idCompra}? Se descontará la mercadería del stock.');">
                                            <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                                            <input type="hidden" name="accion" value="anular">
                                            <input type="hidden" name="idCompra" value="${co.idCompra}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">
                                                <i class="bi bi-x-circle me-1"></i> Anular
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty compras}">
                            <tr><td colspan="6">Todavía no hay compras registradas.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</main>

<script>
    const productos = [
        <%
            List<Producto> productos = (List<Producto>) request.getAttribute("productos");
            if (productos != null) {
                for (int i = 0; i < productos.size(); i++) {
                    Producto p = productos.get(i);
        %>
        {
            id: <%= p.getIdProducto() %>,
            nombre: "<%= js(p.getCodigoSku()) %> - <%= js(p.getNombre()) %>",
            stock: <%= p.getStockActual() %>
        }<%= i < productos.size() - 1 ? "," : "" %>
        <%      }
            } %>
    ];

    function crearCampo(clase, texto, control) {
        const campo = document.createElement("div");
        campo.className = "compra-campo " + clase;
        const etiqueta = document.createElement("label");
        etiqueta.className = "form-label";
        etiqueta.textContent = texto;
        campo.appendChild(etiqueta);
        campo.appendChild(control);
        return campo;
    }

    function agregarProducto() {
        if (productos.length === 0) {
            alert("No hay productos activos registrados.");
            return;
        }

        const fila = document.createElement("div");
        fila.className = "compra-fila";

        const selector = document.createElement("select");
        selector.name = "idProducto";
        selector.className = "form-select";
        selector.required = true;
        productos.forEach(p => {
            const opcion = document.createElement("option");
            opcion.value = p.id;
            opcion.textContent = p.nombre + " | Stock actual: " + p.stock;
            selector.appendChild(opcion);
        });

        const cantidad = document.createElement("input");
        cantidad.type = "number";
        cantidad.name = "cantidad";
        cantidad.className = "form-control";
        cantidad.min = "1";
        cantidad.value = "1";
        cantidad.required = true;

        const precio = document.createElement("input");
        precio.type = "number";
        precio.name = "precioUnitario";
        precio.className = "form-control";
        precio.min = "0";
        precio.step = "0.01";
        precio.placeholder = "0.00";
        precio.required = true;

        const botonEliminar = document.createElement("button");
        botonEliminar.type = "button";
        botonEliminar.className = "btn btn-danger btn-eliminar-producto";
        botonEliminar.textContent = "Eliminar";
        botonEliminar.onclick = function () {
            fila.remove();
            calcularTotal();
        };

        cantidad.oninput = calcularTotal;
        precio.oninput = calcularTotal;

        fila.appendChild(crearCampo("compra-campo-producto", "Producto", selector));
        fila.appendChild(crearCampo("", "Cantidad", cantidad));
        fila.appendChild(crearCampo("", "Precio de compra (S/)", precio));
        fila.appendChild(botonEliminar);

        document.getElementById("filasProductos").appendChild(fila);
        calcularTotal();
    }

    function calcularTotal() {
        let total = 0;
        document.querySelectorAll("#filasProductos .compra-fila").forEach(fila => {
            const cantidad = Number(fila.querySelector("[name=cantidad]").value);
            const precio = Number(fila.querySelector("[name=precioUnitario]").value);
            if (cantidad > 0 && precio >= 0) {
                total += cantidad * precio;
            }
        });
        document.getElementById("totalCompra").textContent = total.toFixed(2);
    }

    document.getElementById("formCompra").addEventListener("submit", function (e) {
        const filas = document.querySelectorAll("#filasProductos .compra-fila");

        if (filas.length === 0) {
            e.preventDefault();
            alert("Debes agregar al menos un producto.");
            return;
        }

        const seleccionados = new Set();
        for (const fila of filas) {
            const id = Number(fila.querySelector("select").value);
            const cantidad = Number(fila.querySelector("[name=cantidad]").value);
            const precio = Number(fila.querySelector("[name=precioUnitario]").value);

            if (!Number.isInteger(cantidad) || cantidad <= 0 || !(precio >= 0)) {
                e.preventDefault();
                alert("Verifica las cantidades y los precios.");
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
