<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Usuarios | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
            <jsp:param name="activa" value="usuarios"/>
        </jsp:include>

        <main>
            <div class="encabezado-pagina">
                <div class="container-fluid px-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div>
                        <h1>Usuarios del personal</h1>
                        <p>Solo el administrador crea las cuentas del personal y les asigna su rol.</p>
                    </div>
                    <button type="button" class="btn btn-es" data-bs-toggle="modal" data-bs-target="#modalCrear">
                        <i class="bi bi-person-plus"></i>Nuevo usuario
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
                           placeholder="Buscar por nombre o correo..." aria-label="Buscar usuario">
                    <select class="form-select" id="filtroRol" aria-label="Filtrar por rol">
                        <option value="">Todos los roles</option>
                        <option value="ADMINISTRADOR">Administrador</option>
                        <option value="VENDEDOR">Vendedor</option>
                    </select>
                </div>

                <div class="panel-es p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 tabla-es" id="tablaUsuarios">
                            <thead>
                                <tr>
                                    <th>Nombres</th>
                                    <th>Correo</th>
                                    <th>Rol</th>
                                    <th>Estado</th>
                                    <th class="text-end">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="u" items="${usuarios}">
                                    <c:set var="soyYo" value="${u.idUsuario == sessionScope.usuario.idUsuario}"/>
                                    <tr>
                                        <td>
                                            <c:out value="${u.nombres}"/>
                                            <c:if test="${soyYo}"><span class="badge text-bg-secondary ms-1">Usted</span></c:if>
                                        </td>
                                        <td><c:out value="${u.correo}"/></td>
                                        <td>
                                            <span class="badge ${u.administrador ? 'text-bg-dark' : 'text-bg-secondary'}">
                                                <c:out value="${u.rol}"/>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge ${u.estado ? 'text-bg-success' : 'text-bg-danger'}">
                                                ${u.estado ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </td>
                                        <td class="text-end text-nowrap">
                                            <button type="button" class="btn btn-sm btn-es-outline btn-editar"
                                                    data-bs-toggle="modal" data-bs-target="#modalEditar"
                                                    data-id="${u.idUsuario}"
                                                    data-nombres="<c:out value='${u.nombres}'/>"
                                                    data-correo="<c:out value='${u.correo}'/>"
                                                    data-rol="${u.rol}"
                                                    data-yo="${soyYo}">
                                                <i class="bi bi-pencil"></i>Editar
                                            </button>
                                            <button type="button" class="btn btn-sm btn-es-outline btn-clave"
                                                    data-bs-toggle="modal" data-bs-target="#modalClave"
                                                    data-id="${u.idUsuario}"
                                                    data-nombres="<c:out value='${u.nombres}'/>">
                                                <i class="bi bi-key"></i>Contraseña
                                            </button>
                                            <c:if test="${not soyYo}">
                                                <form method="post" action="${ctx}/interno/usuarios" class="d-inline"
                                                      onsubmit="return confirm('¿${u.estado ? 'Desactivar' : 'Activar'} esta cuenta?');">
                                                    <input type="hidden" name="csrf" value="${csrf}">
                                                    <input type="hidden" name="accion" value="estado">
                                                    <input type="hidden" name="id" value="${u.idUsuario}">
                                                    <input type="hidden" name="activo" value="${u.estado ? '0' : '1'}">
                                                    <button type="submit" class="btn btn-sm ${u.estado ? 'btn-outline-danger' : 'btn-outline-success'}">
                                                        <i class="bi ${u.estado ? 'bi-person-slash' : 'bi-person-check'}"></i>${u.estado ? 'Desactivar' : 'Activar'}
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty usuarios}">
                                    <tr><td colspan="5" class="text-center text-secondary py-4">No hay usuarios registrados.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <p class="text-secondary small mt-3 mb-0">
                    <i class="bi bi-info-circle me-1"></i>
                    Las cuentas de clientes se crean solas desde la tienda en línea y no aparecen aquí.
                </p>
            </div>
        </main>

        <%-- ================= Modal: nuevo usuario ================= --%>
        <div class="modal fade" id="modalCrear" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <form class="modal-content" method="post" action="${ctx}/interno/usuarios" autocomplete="off">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden" name="accion" value="crear">
                    <div class="modal-header">
                        <h2 class="modal-title h5">Nuevo usuario</h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label" for="cNombres">Nombres y apellidos</label>
                            <input class="form-control" id="cNombres" name="nombres" maxlength="100" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="cCorreo">Correo (será su usuario)</label>
                            <input type="email" class="form-control" id="cCorreo" name="correo" maxlength="120" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="cRol">Rol</label>
                            <select class="form-select" id="cRol" name="rol" required>
                                <option value="VENDEDOR" selected>Vendedor</option>
                                <option value="ADMINISTRADOR">Administrador</option>
                            </select>
                        </div>
                        <div class="mb-1">
                            <label class="form-label" for="cClave">Contraseña temporal</label>
                            <input type="password" class="form-control" id="cClave" name="clave" maxlength="100"
                                   minlength="8" autocomplete="new-password" required>
                            <div class="form-text">Mínimo 8 caracteres, con letras y números.</div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-es">Crear usuario</button>
                    </div>
                </form>
            </div>
        </div>

        <%-- ================= Modal: editar usuario ================= --%>
        <div class="modal fade" id="modalEditar" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <form class="modal-content" method="post" action="${ctx}/interno/usuarios">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden" name="accion" value="editar">
                    <input type="hidden" name="id" id="eId">
                    <div class="modal-header">
                        <h2 class="modal-title h5">Editar usuario</h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label" for="eNombres">Nombres y apellidos</label>
                            <input class="form-control" id="eNombres" name="nombres" maxlength="100" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="eCorreo">Correo</label>
                            <input class="form-control" id="eCorreo" disabled>
                            <div class="form-text">El correo no se puede cambiar.</div>
                        </div>
                        <div class="mb-1">
                            <label class="form-label" for="eRol">Rol</label>
                            <select class="form-select" id="eRol" name="rol" required>
                                <option value="VENDEDOR">Vendedor</option>
                                <option value="ADMINISTRADOR">Administrador</option>
                            </select>
                            <div class="form-text d-none" id="eRolAviso">No puede cambiar su propio rol.</div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-es">Guardar cambios</button>
                    </div>
                </form>
            </div>
        </div>

        <%-- ================= Modal: restablecer contraseña ================= --%>
        <div class="modal fade" id="modalClave" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <form class="modal-content" method="post" action="${ctx}/interno/usuarios" autocomplete="off">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden" name="accion" value="clave">
                    <input type="hidden" name="id" id="kId">
                    <div class="modal-header">
                        <h2 class="modal-title h5">Restablecer contraseña</h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <p>Nueva contraseña para <strong id="kNombres"></strong>.</p>
                        <label class="form-label" for="kClave">Nueva contraseña</label>
                        <input type="password" class="form-control" id="kClave" name="clave" maxlength="100"
                               minlength="8" autocomplete="new-password" required>
                        <div class="form-text">Mínimo 8 caracteres, con letras y números.</div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-es">Guardar contraseña</button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>

        <script>
            // Rellena los modales con los datos de la fila pulsada
            document.querySelectorAll('.btn-editar').forEach(function (b) {
                b.addEventListener('click', function () {
                    document.getElementById('eId').value = b.dataset.id;
                    document.getElementById('eNombres').value = b.dataset.nombres;
                    document.getElementById('eCorreo').value = b.dataset.correo;
                    var rol = document.getElementById('eRol');
                    rol.value = b.dataset.rol;
                    var esYo = b.dataset.yo === 'true';
                    rol.disabled = esYo;          // nadie cambia su propio rol
                    document.getElementById('eRolAviso').classList.toggle('d-none', !esYo);
                });
            });
            document.querySelectorAll('.btn-clave').forEach(function (b) {
                b.addEventListener('click', function () {
                    document.getElementById('kId').value = b.dataset.id;
                    document.getElementById('kNombres').textContent = b.dataset.nombres;
                    document.getElementById('kClave').value = '';
                });
            });
            // Búsqueda y filtro por rol (se aplican en el navegador, sin recargar)
            function filtrarUsuarios() {
                var texto = document.getElementById('buscar').value.toLowerCase();
                var rol = document.getElementById('filtroRol').value;
                document.querySelectorAll('#tablaUsuarios tbody tr').forEach(function (fila) {
                    var celdas = fila.querySelectorAll('td');
                    if (celdas.length < 5) { return; }
                    var coincideTexto = (celdas[0].textContent + ' ' + celdas[1].textContent).toLowerCase().indexOf(texto) !== -1;
                    var coincideRol = rol === '' || celdas[2].textContent.trim() === rol;
                    fila.style.display = (coincideTexto && coincideRol) ? '' : 'none';
                });
            }
            document.getElementById('buscar').addEventListener('input', filtrarUsuarios);
            document.getElementById('filtroRol').addEventListener('change', filtrarUsuarios);

            // Limpia el formulario de "nuevo usuario" cada vez que se abre
            document.getElementById('modalCrear').addEventListener('show.bs.modal', function () {
                this.querySelector('form').reset();
            });
        </script>
    </body>
</html>
