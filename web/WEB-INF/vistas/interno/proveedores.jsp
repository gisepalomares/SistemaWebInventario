<%-- 
    Document   : proveedores
    Created on : 11 oct. 2026, 2:54:05 p. m.
    Author     : Alex
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Proveedores | JD&amp;A Electro Speed</title>

    <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    <link href="${ctx}/css/acceso.css" rel="stylesheet">
</head>

<body>

    <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
        <jsp:param name="activa" value="proveedores"/>
    </jsp:include>

    <main>

        <div class="encabezado-pagina">
            <div class="container-fluid px-4 d-flex flex-wrap
                        justify-content-between align-items-center gap-3">

                <div>
                    <h1>Gestión de Proveedores</h1>
                    <p>Administra los proveedores de ElectroSpeed.</p>
                </div>

                <a href="${ctx}/interno/proveedores#formulario-proveedor"
                   class="btn btn-es">
                    <i class="bi bi-plus-circle me-1"></i>
                    Nuevo proveedor
                </a>

            </div>
        </div>

        <div class="container-fluid px-4 pb-5">

            
<c:if test="${not empty ok}">
    <div class="alert alert-success">
        <i class="bi bi-check-circle me-1"></i>
        <c:out value="${ok}"/>
    </div>
</c:if>

<c:if test="${not empty error}">
    <div class="alert alert-danger">
        <i class="bi bi-exclamation-triangle me-1"></i>
        <c:out value="${error}"/>
    </div>
</c:if>


            <div class="barra-filtros">

                <form method="get"
                      action="${ctx}/interno/proveedores"
                      class="d-flex gap-2 w-100">

                    <input type="search"
                           class="form-control"
                           name="buscar"
                           placeholder="Buscar por RUC o razón social"
                           value="<c:out value='${param.buscar}'/>">

                    <button type="submit" class="btn btn-es">
                        <i class="bi bi-search"></i>
                        Buscar
                    </button>

                    <a href="${ctx}/interno/proveedores"
                       class="btn btn-es-outline">
                        Limpiar
                    </a>

                </form>

            </div>

            <div class="panel-es p-0">

                <div class="table-responsive">

                    <table class="table table-hover align-middle
                                  mb-0 tabla-es">

                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>RUC</th>
                                <th>Razón social</th>
                                <th>Nombre comercial</th>
                                <th>Teléfono</th>
                                <th>Correo</th>
                                <th class="text-end">Acciones</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:forEach var="p" items="${proveedores}">

                                <tr>
                                    <td><c:out value="${p.idProveedor}"/></td>
                                    <td><c:out value="${p.ruc}"/></td>
                                    <td><c:out value="${p.razonSocial}"/></td>
                                    <td><c:out value="${p.nombreComercial}"/></td>
                                    <td><c:out value="${p.telefono}"/></td>
                                    <td><c:out value="${p.correo}"/></td>

                                    
            <td class="text-end">
               <div class="d-flex justify-content-end gap-2">

                    <a class="btn btn-sm btn-es-outline"
                       style="width: 85px;"
                       href="${ctx}/interno/proveedores?accion=editar&amp;id=${p.idProveedor}">
                        <i class="bi bi-pencil"></i>
                        Editar
                    </a>

                    <form method="post"
                        action="${ctx}/interno/proveedores"
                        id="form-eliminar-${p.idProveedor}">

                      <input type="hidden" name="csrf" value="${csrf}">
                      <input type="hidden" name="accion" value="eliminar">
                      <input type="hidden" name="idProveedor"
                             value="${p.idProveedor}">

                      <button type="button"
                              class="btn btn-sm btn-danger"
                              style="width: 85px;"
                              data-bs-toggle="modal"
                              data-bs-target="#modalEliminar"
                              data-form-id="form-eliminar-${p.idProveedor}"
                              data-proveedor="${p.razonSocial}">
                          <i class="bi bi-trash"></i>
                          Eliminar
                      </button>
                  </form>
                </div>
            </td>

                                </tr>

                            </c:forEach>

                            <c:if test="${empty proveedores}">
                                <tr>
                                    <td colspan="7"
                                        class="text-center text-secondary py-4">
                                        No se encontraron proveedores.
                                    </td>
                                </tr>
                            </c:if>

                        </tbody>

                    </table>

                </div>

            </div>

            <div id="formulario-proveedor" class="panel-es p-4 mt-4">

                <h2 class="h5 mb-3">
                    <c:choose>
                        <c:when test="${not empty proveedor}">
                            Editar proveedor
                        </c:when>
                        <c:otherwise>
                            Registrar proveedor
                        </c:otherwise>
                    </c:choose>
                </h2>

                <form method="post"
                      action="${ctx}/interno/proveedores">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden"
                           name="accion"
                           value="${empty proveedor ? 'registrar' : 'actualizar'}">

                    <c:if test="${not empty proveedor}">
                        <input type="hidden"
                               name="idProveedor"
                               value="${proveedor.idProveedor}">
                    </c:if>

                    <div class="row g-3">

                        <div class="col-md-6">
                            <label class="form-label">RUC</label>
                            <input class="form-control"
                                   name="ruc"
                                   maxlength="11"
                                   pattern="[0-9]{11}"
                                   required
                                   value="<c:out value='${proveedor.ruc}'/>">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Razón social</label>
                            <input class="form-control"
                                   name="razonSocial"
                                   maxlength="120"
                                   required
                                   value="<c:out value='${proveedor.razonSocial}'/>">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Nombre comercial</label>
                            <input class="form-control"
                                   name="nombreComercial"
                                   maxlength="120"
                                   value="<c:out value='${proveedor.nombreComercial}'/>">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Dirección</label>
                            <input class="form-control"
                                   name="direccion"
                                   maxlength="200"
                                   value="<c:out value='${proveedor.direccion}'/>">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Persona de contacto</label>
                            <input class="form-control"
                                   name="personaContacto"
                                   maxlength="100"
                                   value="<c:out value='${proveedor.personaContacto}'/>">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Teléfono</label>
                            <input class="form-control"
                                   name="telefono"
                                   maxlength="20"
                                   value="<c:out value='${proveedor.telefono}'/>">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Correo</label>
                            <input type="email"
                                   class="form-control"
                                   name="correo"
                                   maxlength="120"
                                   value="<c:out value='${proveedor.correo}'/>">
                        </div>

                    </div>

                    <div class="mt-4 d-flex gap-2">

                        <button type="submit" class="btn btn-es">
                            <i class="bi bi-save me-1"></i>
                            ${empty proveedor ? 'Registrar' : 'Guardar cambios'}
                        </button>

                        <c:if test="${not empty proveedor}">
                            <a href="${ctx}/interno/proveedores"
                               class="btn btn-es-outline">
                                Cancelar
                            </a>
                        </c:if>

                    </div>

                </form>

            </div>

        </div>

    </main>

    <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>

<!-- Modal de confirmación para eliminar proveedor -->
<div class="modal fade" id="modalEliminar"
     tabindex="-1"
     aria-labelledby="modalEliminarTitulo"
     aria-hidden="true">

    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">

            <div class="modal-header">
                <h5 class="modal-title" id="modalEliminarTitulo">
                    <i class="bi bi-exclamation-triangle text-danger me-2"></i>
                    Eliminar proveedor
                </h5>
                <button type="button"
                        class="btn-close"
                        data-bs-dismiss="modal"
                        aria-label="Cerrar"></button>
            </div>

            <div class="modal-body">
                <p>
                    ¿Estás seguro de eliminar al proveedor
                    <strong id="nombreProveedorEliminar"></strong>?
                </p>
                <p class="text-danger mb-0">
                    Esta acción no se puede deshacer.
                </p>
            </div>

            <div class="modal-footer">
                <button type="button"
                        class="btn btn-es-outline"
                        data-bs-dismiss="modal">
                    Cancelar
                </button>

                <button type="button"
                        class="btn btn-danger"
                        id="confirmarEliminar">
                    <i class="bi bi-trash me-1"></i>
                    Sí, eliminar
                </button>
            </div>

        </div>
    </div>
</div>

<script>
    document.getElementById('modalEliminar')
        .addEventListener('show.bs.modal', function (event) {

            const boton = event.relatedTarget;

            const formularioId =
                boton.getAttribute('data-form-id');

            const nombre =
                boton.getAttribute('data-proveedor');

            document.getElementById(
                'nombreProveedorEliminar'
            ).textContent = nombre;

            document.getElementById(
                'confirmarEliminar'
            ).onclick = function () {
                document.getElementById(formularioId).submit();
            };
        });
</script>

</body>
</html>


