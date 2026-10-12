<%-- 
    Document   : categorias
    Created on : 11 oct. 2026, 4:26:54 p. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Categorías | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
            <jsp:param name="activa" value="categorias"/>
        </jsp:include>

        <main>
            <div class="encabezado-pagina">
                <div class="container-fluid px-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div>
                        <h1>Categorías</h1>
                        <p>Clasificación de los productos. Solo el administrador puede modificarla.</p>
                    </div>
                    <button type="button" class="btn btn-es" data-bs-toggle="modal" data-bs-target="#modalCrear">
                        <i class="bi bi-plus-lg"></i>Nueva categoría
                    </button>
                </div>
            </div>

            <div class="container-fluid px-4 pb-5">

                <c:if test="${not empty ok}">
                    <div class="alert alert-success"><i class="bi bi-check-circle me-1"></i><c:out value="${ok}"/></div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-1"></i><c:out value="${error}"/></div>
                </c:if>

                <div class="barra-filtros">
                    <input type="search" class="form-control" id="buscar"
                           placeholder="Buscar categoría..." aria-label="Buscar categoría">
                </div>

                <div class="panel-es p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 tabla-es" id="tablaCategorias">
                            <thead>
                                <tr>
                                    <th>Nombre</th>
                                    <th>Descripción</th>
                                    <th class="text-center">Productos</th>
                                    <th class="text-end">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="cat" items="${categorias}">
                                    <tr>
                                        <td><strong><c:out value="${cat.nombre}"/></strong></td>
                                        <td><c:out value="${empty cat.descripcion ? '—' : cat.descripcion}"/></td>
                                        <td class="text-center">
                                            <span class="badge ${cat.totalProductos gt 0 ? 'text-bg-secondary' : 'text-bg-light border'}">${cat.totalProductos}</span>
                                        </td>
                                        <td class="text-end text-nowrap">
                                            <button type="button" class="btn btn-sm btn-es-outline btn-editar"
                                                    data-bs-toggle="modal" data-bs-target="#modalEditar"
                                                    data-id="${cat.idCategoria}"
                                                    data-nombre="<c:out value='${cat.nombre}'/>"
                                                    data-descripcion="<c:out value='${cat.descripcion}'/>">
                                                <i class="bi bi-pencil"></i>Editar
                                            </button>
                                            <c:choose>
                                                <c:when test="${cat.totalProductos gt 0}">
                                                    <span class="d-inline-block" tabindex="0" title="Tiene productos asociados; no se puede eliminar.">
                                                        <button type="button" class="btn btn-sm btn-outline-danger" disabled>
                                                            <i class="bi bi-trash"></i>Eliminar
                                                        </button>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <form method="post" action="${ctx}/interno/categorias" class="d-inline"
                                                          onsubmit="return confirm('¿Eliminar esta categoría? Esta acción no se puede deshacer.');">
                                                        <input type="hidden" name="csrf" value="${csrf}">
                                                        <input type="hidden" name="accion" value="eliminar">
                                                        <input type="hidden" name="id" value="${cat.idCategoria}">
                                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                                            <i class="bi bi-trash"></i>Eliminar
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty categorias}">
                                    <tr><td colspan="4" class="text-center text-secondary py-4">No hay categorías registradas.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <p class="text-secondary small mt-3 mb-0">
                    <i class="bi bi-info-circle me-1"></i>
                    Una categoría solo se puede eliminar cuando no tiene productos (activos ni inactivos).
                </p>
            </div>
        </main>

        <%-- ================= Modal: nueva categoría ================= --%>
        <div class="modal fade" id="modalCrear" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <form class="modal-content" method="post" action="${ctx}/interno/categorias" autocomplete="off">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden" name="accion" value="crear">
                    <div class="modal-header">
                        <h2 class="modal-title h5">Nueva categoría</h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label" for="cNombre">Nombre</label>
                            <input class="form-control" id="cNombre" name="nombre" minlength="3" maxlength="60" required>
                        </div>
                        <div class="mb-1">
                            <label class="form-label" for="cDescripcion">Descripción <span class="text-secondary fw-normal">(opcional)</span></label>
                            <textarea class="form-control" id="cDescripcion" name="descripcion" rows="2" maxlength="200"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-es">Registrar categoría</button>
                    </div>
                </form>
            </div>
        </div>

        <%-- ================= Modal: editar categoría ================= --%>
        <div class="modal fade" id="modalEditar" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <form class="modal-content" method="post" action="${ctx}/interno/categorias" autocomplete="off">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden" name="accion" value="editar">
                    <input type="hidden" name="id" id="eId">
                    <div class="modal-header">
                        <h2 class="modal-title h5">Editar categoría</h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label" for="eNombre">Nombre</label>
                            <input class="form-control" id="eNombre" name="nombre" minlength="3" maxlength="60" required>
                        </div>
                        <div class="mb-1">
                            <label class="form-label" for="eDescripcion">Descripción <span class="text-secondary fw-normal">(opcional)</span></label>
                            <textarea class="form-control" id="eDescripcion" name="descripcion" rows="2" maxlength="200"></textarea>
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
                 data-nombre="<c:out value='${form.nombre}'/>"
                 data-descripcion="<c:out value='${form.descripcion}'/>"></div>
        </c:if>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>

        <script>
            var reabriendo = false;

            function rellenarEditar(b) {
                document.getElementById('eId').value = b.dataset.id;
                document.getElementById('eNombre').value = b.dataset.nombre;
                document.getElementById('eDescripcion').value = b.dataset.descripcion;
            }
            document.querySelectorAll('.btn-editar').forEach(function (b) {
                b.addEventListener('click', function () { rellenarEditar(b); });
            });

            var modalCrear = document.getElementById('modalCrear');
            modalCrear.addEventListener('show.bs.modal', function () {
                if (!reabriendo) { this.querySelector('form').reset(); }
            });
            modalCrear.addEventListener('shown.bs.modal', function () { reabriendo = false; });

            // Búsqueda en el navegador, sin recargar
            document.getElementById('buscar').addEventListener('input', function () {
                var texto = this.value.toLowerCase();
                document.querySelectorAll('#tablaCategorias tbody tr').forEach(function (fila) {
                    var celdas = fila.querySelectorAll('td');
                    if (celdas.length < 4) { return; }
                    var coincide = (celdas[0].textContent + ' ' + celdas[1].textContent).toLowerCase().indexOf(texto) !== -1;
                    fila.style.display = coincide ? '' : 'none';
                });
            });

            // Si el servidor rechazó el formulario, se reabre con lo que el usuario escribió
            (function () {
                var prev = document.getElementById('formPrevio');
                if (!prev) { return; }
                var d = prev.dataset;
                if (d.accion === 'crear') {
                    reabriendo = true;
                    document.getElementById('cNombre').value = d.nombre;
                    document.getElementById('cDescripcion').value = d.descripcion;
                    bootstrap.Modal.getOrCreateInstance(modalCrear).show();
                } else if (d.accion === 'editar') {
                    document.getElementById('eId').value = d.id;
                    document.getElementById('eNombre').value = d.nombre;
                    document.getElementById('eDescripcion').value = d.descripcion;
                    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalEditar')).show();
                }
            })();
        </script>
    </body>
</html>
