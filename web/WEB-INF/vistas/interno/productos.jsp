<%-- 
    Document   : productos
    Created on : 11 oct. 2026, 4:25:43 p. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="es_PE"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="esAdmin" value="${sessionScope.usuario.administrador}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Productos | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">

        <style>
            .fila-inactiva td {
                background-color: #f1f1f1;
                color: #6c757d;
            }
        </style>

    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
            <jsp:param name="activa" value="productos"/>
        </jsp:include>

        <main>
            <div class="encabezado-pagina">
                <div class="container-fluid px-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div>
                        <h1>Productos</h1>
                        <p>
                            <c:choose>
                                <c:when test="${esAdmin}">Registre y mantenga el inventario, los precios y el stock mínimo de cada producto.</c:when>
                                <c:otherwise>Consulta del inventario y de la disponibilidad de cada producto.</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                    <c:if test="${esAdmin}">
                        <button type="button" class="btn btn-es" data-bs-toggle="modal" data-bs-target="#modalCrear">
                            <i class="bi bi-plus-lg"></i>Nuevo producto
                        </button>
                    </c:if>
                </div>
            </div>

            <div class="container-fluid px-4 pb-5">

                <c:if test="${not empty ok}">
                    <div class="alert alert-success"><i class="bi bi-check-circle me-1"></i><c:out value="${ok}"/></div>
                    </c:if>
                    <c:if test="${not empty error}">
                    <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-1"></i><c:out value="${error}"/></div>
                    </c:if>

                <%-- ===== Resumen de alertas de stock ===== --%>
                <div class="row g-3 mb-3">
                    <div class="col-12 col-sm-4">
                        <a class="panel-es d-flex align-items-center gap-3 text-decoration-none" href="${ctx}/interno/productos">
                            <i class="bi bi-box-seam fs-3 text-secondary"></i>
                            <div>
                                <div class="fs-4 fw-bold lh-1 text-dark">${totalListado}</div>
                                <div class="small text-secondary">Productos en la lista</div>
                            </div>
                        </a>
                    </div>
                    <div class="col-12 col-sm-4">
                        <a class="panel-es d-flex align-items-center gap-3 text-decoration-none" href="${ctx}/interno/productos?stock=bajo">
                            <i class="bi bi-exclamation-triangle fs-3 text-warning"></i>
                            <div>
                                <div class="fs-4 fw-bold lh-1 text-dark">${totalBajo}</div>
                                <div class="small text-secondary">Con stock bajo el mínimo</div>
                            </div>
                        </a>
                    </div>
                    <div class="col-12 col-sm-4">
                        <a class="panel-es d-flex align-items-center gap-3 text-decoration-none" href="${ctx}/interno/productos?stock=agotado">
                            <i class="bi bi-x-octagon fs-3 text-danger"></i>
                            <div>
                                <div class="fs-4 fw-bold lh-1 text-dark">${totalAgotado}</div>
                                <div class="small text-secondary">Agotados</div>
                            </div>
                        </a>
                    </div>
                </div>

                <%-- ===== Filtros ===== --%>
                <form method="get" action="${ctx}/interno/productos" class="barra-filtros">
                    <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                           placeholder="Buscar por nombre o código (SKU)..." aria-label="Buscar producto">
                    <select class="form-select" name="categoria" aria-label="Filtrar por categoría">
                        <option value="">Todas las categorías</option>
                        <c:forEach var="c" items="${categorias}">
                            <option value="${c.idCategoria}" ${c.idCategoria == categoriaSel ? 'selected' : ''}>
                                <c:out value="${c.nombre}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${esAdmin}">
                        <select class="form-select" name="estado" aria-label="Filtrar por estado">
                            <option value="activos" ${estadoSel eq 'activos' ? 'selected' : ''}>Solo activos</option>
                            <option value="inactivos" ${estadoSel eq 'inactivos' ? 'selected' : ''}>Solo inactivos</option>
                            <option value="todos" ${estadoSel eq 'todos' ? 'selected' : ''}>Todos</option>
                        </select>
                    </c:if>
                    <select class="form-select" name="stock" aria-label="Filtrar por stock">
                        <option value="">Todo el stock</option>
                        <option value="bajo" ${stockSel eq 'bajo' ? 'selected' : ''}>Stock bajo</option>
                        <option value="agotado" ${stockSel eq 'agotado' ? 'selected' : ''}>Agotados</option>
                    </select>
                    <button type="submit" class="btn btn-es"><i class="bi bi-search"></i>Filtrar</button>
                    <a class="btn btn-es-outline" href="${ctx}/interno/productos">Limpiar</a>
                </form>

                <%-- ===== Tabla ===== --%>
                <div class="panel-es p-0">
                    <div class="table-responsive">
                        <table id="tablaProductos" class="table table-hover align-middle mb-0 tabla-es w-100">
                            <thead>
                                <tr>
                                    <th>Código</th>
                                    <th>Producto</th>
                                    <th>Categoría</th>
                                    <th>Proveedor</th>
                                    <th class="text-end">Precio</th>
                                    <th class="text-center">Disponible</th>
                                    <th class="text-center">Mínimo</th>
                                    <th>Estado</th>
                                    <c:if test="${esAdmin}"><th class="text-end">Acciones</th></c:if>
                                    </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="p" items="${productos}">
                                    <c:set var="clase" value="${not p.estado ? 'fila-inactiva' : (p.agotado ? 'table-danger' : (p.stockBajo ? 'table-warning' : ''))}"/>
                                    <tr class="${clase}">
                                        <td class="text-nowrap"><c:out value="${p.codigoSku}"/></td>
                                        <td>
                                            <strong><c:out value="${p.nombre}"/></strong>
                                            <c:if test="${not empty p.descripcion}">
                                                <div class="small text-secondary"><c:out value="${p.descripcion}"/></div>
                                            </c:if>
                                        </td>
                                        <td><c:out value="${p.categoria}"/></td>
                                        <td><c:out value="${empty p.proveedor ? '—' : p.proveedor}"/></td>
                                        <td class="text-end text-nowrap">S/ <fmt:formatNumber value="${p.precioVenta}" pattern="#,##0.00"/></td>
                                        <td class="text-center">
                                            <strong>${p.stockDisponible}</strong>
                                            <c:if test="${p.estado}">
                                                <c:choose>
                                                    <c:when test="${p.agotado}"><span class="badge text-bg-danger ms-1">Agotado</span></c:when>
                                                    <c:when test="${p.stockBajo}"><span class="badge text-bg-warning ms-1">Stock bajo</span></c:when>
                                                </c:choose>
                                            </c:if>
                                            <div class="small text-secondary">
                                                Físico: ${p.stockActual}<c:if test="${p.stockReservado gt 0}"> · Reservado: ${p.stockReservado}</c:if>
                                                </div>
                                            </td>
                                            <td class="text-center">${p.stockMinimo}</td>
                                        <td>
                                            <span class="badge ${p.estado ? 'text-bg-success' : 'text-bg-secondary'}">
                                                ${p.estado ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </td>
                                        <c:if test="${esAdmin}">
                                            <td class="text-end text-nowrap">
                                                <button type="button" class="btn btn-sm btn-es-outline btn-editar"
                                                        data-bs-toggle="modal" data-bs-target="#modalEditar"
                                                        data-id="${p.idProducto}"
                                                        data-sku="<c:out value='${p.codigoSku}'/>"
                                                        data-nombre="<c:out value='${p.nombre}'/>"
                                                        data-descripcion="<c:out value='${p.descripcion}'/>"
                                                        data-precio="${p.precioVenta}"
                                                        data-categoria="${p.idCategoria}"
                                                        data-proveedor="${p.idProveedor}"
                                                        data-minimo="${p.stockMinimo}"
                                                        data-stock="${p.stockActual}"
                                                        data-reservado="${p.stockReservado}">
                                                    <i class="bi bi-pencil"></i>Editar
                                                </button>
                                                <form method="post" action="${ctx}/interno/productos" class="d-inline"
                                                      onsubmit="return confirm('¿${p.estado ? 'Desactivar' : 'Activar'} este producto?');">
                                                    <input type="hidden" name="csrf" value="${csrf}">
                                                    <input type="hidden" name="accion" value="estado">
                                                    <input type="hidden" name="id" value="${p.idProducto}">
                                                    <input type="hidden" name="activo" value="${p.estado ? '0' : '1'}">
                                                    <button type="submit" class="btn btn-sm ${p.estado ? 'btn-outline-danger' : 'btn-outline-success'}">
                                                        <i class="bi ${p.estado ? 'bi-slash-circle' : 'bi-check-circle'}"></i>${p.estado ? 'Desactivar' : 'Activar'}
                                                    </button>
                                                </form>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>

                            </tbody>
                        </table>
                    </div>
                </div>

                <p class="text-secondary small mt-3 mb-0">
                    <i class="bi bi-info-circle me-1"></i>
                    <strong>Disponible</strong> es el stock físico menos lo reservado por pedidos en línea pendientes.
                    <c:choose>
                        <c:when test="${esAdmin}">Un producto desactivado deja de mostrarse en el catálogo, pero conserva su historial.</c:when>
                        <c:otherwise>Solo el administrador puede registrar o modificar productos.</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </main>

        <c:if test="${esAdmin}">
            <%-- ================= Modal: nuevo producto ================= --%>
            <div class="modal fade" id="modalCrear" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <form class="modal-content" method="post" action="${ctx}/interno/productos" autocomplete="off">
                        <input type="hidden" name="csrf" value="${csrf}">
                        <input type="hidden" name="accion" value="crear">
                        <div class="modal-header">
                            <h2 class="modal-title h5">Nuevo producto</h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label" for="cSku">Código (SKU)</label>
                                    <input class="form-control" id="cSku" name="sku" maxlength="20" required>
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label" for="cNombre">Nombre del producto</label>
                                    <input class="form-control" id="cNombre" name="nombre" maxlength="120" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label" for="cDescripcion">Descripción <span class="text-secondary fw-normal">(opcional)</span></label>
                                    <textarea class="form-control" id="cDescripcion" name="descripcion" rows="2" maxlength="255"></textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="cCategoria">Categoría</label>
                                    <select class="form-select" id="cCategoria" name="categoria" required>
                                        <option value="">Seleccione...</option>
                                        <c:forEach var="c" items="${categorias}">
                                            <option value="${c.idCategoria}"><c:out value="${c.nombre}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="cProveedor">Proveedor <span class="text-secondary fw-normal">(opcional)</span></label>
                                    <select class="form-select" id="cProveedor" name="proveedor">
                                        <option value="">Sin proveedor</option>
                                        <c:forEach var="pr" items="${proveedores}">
                                            <option value="${pr.idProveedor}"><c:out value="${pr.nombreMostrar}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label" for="cPrecio">Precio de venta</label>
                                    <div class="input-group">
                                        <span class="input-group-text">S/</span>
                                        <input type="number" class="form-control" id="cPrecio" name="precio"
                                               min="0.01" max="99999999.99" step="0.01" required>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label" for="cStock">Stock inicial</label>
                                    <input type="number" class="form-control" id="cStock" name="stock"
                                           min="0" max="999999" step="1" value="0" required>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label" for="cMinimo">Stock mínimo</label>
                                    <input type="number" class="form-control" id="cMinimo" name="minimo"
                                           min="0" max="99999" step="1" value="0" required>
                                </div>
                            </div>
                            <div class="form-text mt-3">
                                El stock inicial solo se define al registrar el producto. Después se actualiza con las compras y las ventas.
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-es">Registrar producto</button>
                        </div>
                    </form>
                </div>
            </div>

            <%-- ================= Modal: editar producto ================= --%>
            <div class="modal fade" id="modalEditar" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <form class="modal-content" method="post" action="${ctx}/interno/productos" autocomplete="off">
                        <input type="hidden" name="csrf" value="${csrf}">
                        <input type="hidden" name="accion" value="editar">
                        <input type="hidden" name="id" id="eId">
                        <div class="modal-header">
                            <h2 class="modal-title h5">Editar producto</h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label" for="eSku">Código (SKU)</label>
                                    <input class="form-control" id="eSku" name="sku" maxlength="20" required>
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label" for="eNombre">Nombre del producto</label>
                                    <input class="form-control" id="eNombre" name="nombre" maxlength="120" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label" for="eDescripcion">Descripción <span class="text-secondary fw-normal">(opcional)</span></label>
                                    <textarea class="form-control" id="eDescripcion" name="descripcion" rows="2" maxlength="255"></textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="eCategoria">Categoría</label>
                                    <select class="form-select" id="eCategoria" name="categoria" required>
                                        <option value="">Seleccione...</option>
                                        <c:forEach var="c" items="${categorias}">
                                            <option value="${c.idCategoria}"><c:out value="${c.nombre}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="eProveedor">Proveedor <span class="text-secondary fw-normal">(opcional)</span></label>
                                    <select class="form-select" id="eProveedor" name="proveedor">
                                        <option value="">Sin proveedor</option>
                                        <c:forEach var="pr" items="${proveedores}">
                                            <option value="${pr.idProveedor}"><c:out value="${pr.nombreMostrar}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="ePrecio">Precio de venta</label>
                                    <div class="input-group">
                                        <span class="input-group-text">S/</span>
                                        <input type="number" class="form-control" id="ePrecio" name="precio"
                                               min="0.01" max="99999999.99" step="0.01" required>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="eMinimo">Stock mínimo</label>
                                    <input type="number" class="form-control" id="eMinimo" name="minimo"
                                           min="0" max="99999" step="1" required>
                                </div>
                            </div>
                            <div class="form-text mt-3">
                                Stock físico actual: <strong id="eStock">0</strong>
                                (reservado por pedidos: <span id="eReservado">0</span>).
                                El stock no se modifica aquí; se actualiza con las compras y las ventas.
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-es">Guardar cambios</button>
                        </div>
                    </form>
                </div>
            </div>

            <%-- Datos que el servidor devuelve cuando el formulario tuvo un error --%>
            <c:if test="${not empty formAccion}">
                <div id="formPrevio" class="d-none"
                     data-accion="<c:out value='${formAccion}'/>"
                     data-id="<c:out value='${form.id}'/>"
                     data-sku="<c:out value='${form.sku}'/>"
                     data-nombre="<c:out value='${form.nombre}'/>"
                     data-descripcion="<c:out value='${form.descripcion}'/>"
                     data-precio="<c:out value='${form.precio}'/>"
                     data-categoria="<c:out value='${form.categoria}'/>"
                     data-proveedor="<c:out value='${form.proveedor}'/>"
                     data-minimo="<c:out value='${form.minimo}'/>"
                     data-stock="<c:out value='${form.stock}'/>"></div>
            </c:if>
        </c:if>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>

        <c:if test="${esAdmin}">



            <script>
                var reabriendo = false;

                // Rellena el modal de edición con los datos de la fila pulsada
                function rellenarEditar(b) {
                    document.getElementById('eId').value = b.dataset.id;
                    document.getElementById('eSku').value = b.dataset.sku;
                    document.getElementById('eNombre').value = b.dataset.nombre;
                    document.getElementById('eDescripcion').value = b.dataset.descripcion;
                    document.getElementById('ePrecio').value = b.dataset.precio;
                    document.getElementById('eCategoria').value = b.dataset.categoria;
                    document.getElementById('eProveedor').value = b.dataset.proveedor;
                    document.getElementById('eMinimo').value = b.dataset.minimo;
                    document.getElementById('eStock').textContent = b.dataset.stock;
                    document.getElementById('eReservado').textContent = b.dataset.reservado;
                }
                document.querySelectorAll('.btn-editar').forEach(function (b) {
                    b.addEventListener('click', function () {
                        rellenarEditar(b);
                    });
                });

                // El formulario de "nuevo producto" se limpia cada vez que se abre
                var modalCrear = document.getElementById('modalCrear');
                modalCrear.addEventListener('show.bs.modal', function () {
                    if (!reabriendo) {
                        this.querySelector('form').reset();
                    }
                });
                modalCrear.addEventListener('shown.bs.modal', function () {
                    reabriendo = false;
                });

                // Si el servidor rechazó el formulario, se reabre con lo que el usuario escribió
                (function () {
                    var prev = document.getElementById('formPrevio');
                    if (!prev) {
                        return;
                    }
                    var d = prev.dataset;
                    if (d.accion === 'crear') {
                        reabriendo = true;
                        document.getElementById('cSku').value = d.sku;
                        document.getElementById('cNombre').value = d.nombre;
                        document.getElementById('cDescripcion').value = d.descripcion;
                        document.getElementById('cPrecio').value = d.precio;
                        document.getElementById('cCategoria').value = d.categoria;
                        document.getElementById('cProveedor').value = d.proveedor;
                        document.getElementById('cStock').value = d.stock;
                        document.getElementById('cMinimo').value = d.minimo;
                        bootstrap.Modal.getOrCreateInstance(modalCrear).show();
                    } else if (d.accion === 'editar') {
                        var fila = document.querySelector('.btn-editar[data-id="' + d.id + '"]');
                        if (fila) {
                            rellenarEditar(fila);
                        } else {
                            document.getElementById('eId').value = d.id;
                        }
                        document.getElementById('eSku').value = d.sku;
                        document.getElementById('eNombre').value = d.nombre;
                        document.getElementById('eDescripcion').value = d.descripcion;
                        document.getElementById('ePrecio').value = d.precio;
                        document.getElementById('eCategoria').value = d.categoria;
                        document.getElementById('eProveedor').value = d.proveedor;
                        document.getElementById('eMinimo').value = d.minimo;
                        bootstrap.Modal.getOrCreateInstance(document.getElementById('modalEditar')).show();
                    }
                })();
            </script>
        </c:if>
<!-- DataTables: librería principal -->
<script src="https://cdn.datatables.net/2.3.4/js/dataTables.min.js"></script>

<!-- Integración con Bootstrap 5 -->
<script src="https://cdn.datatables.net/2.3.4/js/dataTables.bootstrap5.min.js"></script>

<script>
    document.addEventListener("DOMContentLoaded", function () {

        new DataTable("#tablaProductos", {
            pageLength: 10,

            lengthMenu: [
                [10, 25, 50, -1],
                [10, 25, 50, "Todos"]
            ],

            language: {
                url: "https://cdn.datatables.net/plug-ins/2.3.4/i18n/es-ES.json"
            },

            order: [],

            columnDefs: [
                {
                    orderable: false,
                    targets: -1
                }
            ]
        });

    });
    </script>
    </body>
</html>
