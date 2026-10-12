<%-- 
    Document   : pedidos
    Created on : 11 oct. 2026, 7:37:23 p. m.
    Author     : USER
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<fmt:setLocale value="es_PE"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="estados" value="${fn:split('PENDIENTE_PAGO,PAGADO,LISTO_RECOGER,ENTREGADO,VENCIDO,CANCELADO', ',')}"/>
<c:set var="etiquetas" value="${fn:split('Pendientes de pago,Por preparar,Listos para recoger,Entregados,Vencidos,Cancelados', ',')}"/>

<%-- Campos que se repiten en todos los formularios (token + filtros actuales para volver a la misma vista) --%>
<c:set var="camposComunes">
    <input type="hidden" name="csrf" value="${csrf}">
    <input type="hidden" name="estado" value="${estadoSel}">
    <input type="hidden" name="q" value="<c:out value='${q}'/>">
</c:set>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Pedidos en línea | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
            <jsp:param name="activa" value="pedidos"/>
        </jsp:include>

        <main>
            <div class="encabezado-pagina">
                <div class="container-fluid px-4">
                    <h1>Pedidos en línea</h1>
                    <p>Registre el pago en tienda, prepare, entregue o cancele los pedidos de los clientes.</p>
                </div>
            </div>

            <div class="container-fluid px-4 pb-5">

                <c:if test="${not empty ok}">
                    <div class="alert alert-success"><i class="bi bi-check-circle me-1"></i><c:out value="${ok}"/></div>
                    </c:if>
                    <c:if test="${not empty error}">
                    <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-1"></i><c:out value="${error}"/></div>
                    </c:if>

                <%-- ===== Pestañas por estado ===== --%>
                <ul class="nav nav-pills mb-3 gap-1">
                    <li class="nav-item">
                        <c:url var="urlTodos" value="/interno/pedidos"><c:param name="q" value="${q}"/></c:url>
                        <a class="nav-link ${empty estadoSel ? 'active' : ''}" href="${urlTodos}">
                            Todos <span class="badge text-bg-light">${empty conteos['TODOS'] ? 0 : conteos['TODOS']}</span>
                        </a>
                    </li>
                    <c:forEach var="e" items="${estados}" varStatus="st">
                        <li class="nav-item">
                            <c:url var="urlTab" value="/interno/pedidos">
                                <c:param name="estado" value="${e}"/>
                                <c:param name="q" value="${q}"/>
                            </c:url>
                            <a class="nav-link ${estadoSel eq e ? 'active' : ''}" href="${urlTab}">
                                ${etiquetas[st.index]}
                                <span class="badge text-bg-light">${empty conteos[e] ? 0 : conteos[e]}</span>
                            </a>
                        </li>
                    </c:forEach>
                </ul>

                <%-- ===== Búsqueda ===== --%>
                <form method="get" action="${ctx}/interno/pedidos" class="panel-es mb-4">
                    <input type="hidden" name="estado" value="${estadoSel}">
                    <div class="row g-2">
                        <div class="col-md-9">
                            <input type="text" name="q" value="<c:out value='${q}'/>" class="form-control"
                                   placeholder="Buscar por código de recojo, nombre del cliente o DNI">
                        </div>
                        <div class="col-md-3 d-grid">
                            <button type="submit" class="btn btn-es"><i class="bi bi-search"></i>Buscar</button>
                        </div>
                    </div>
                </form>

                <c:if test="${empty error and empty pedidos}">
                    <div class="alert alert-secondary">No hay pedidos con los criterios indicados.</div>
                </c:if>

                <%-- ===== Pedidos ===== --%>
                <c:forEach var="ped" items="${pedidos}">

                    <c:choose>
                        <c:when test="${ped.estado eq 'PENDIENTE_PAGO'}"><c:set var="claseEstado" value="text-bg-warning"/></c:when>
                        <c:when test="${ped.estado eq 'PAGADO'}"><c:set var="claseEstado" value="text-bg-primary"/></c:when>
                        <c:when test="${ped.estado eq 'LISTO_RECOGER'}"><c:set var="claseEstado" value="text-bg-success"/></c:when>
                        <c:when test="${ped.estado eq 'ENTREGADO'}"><c:set var="claseEstado" value="text-bg-secondary"/></c:when>
                        <c:otherwise><c:set var="claseEstado" value="text-bg-danger"/></c:otherwise>
                    </c:choose>

                    <div class="panel-es mb-4">
                        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-2">
                            <div>
                                <p class="text-muted small mb-0">Pedido N.° ${ped.idPedido} · <c:out value="${ped.fechaPedidoTexto}"/></p>
                                <p class="h4 fw-bold mb-0">Código: <c:out value="${ped.codigoRecojo}"/></p>
                            </div>
                            <span class="badge fs-6 ${claseEstado}"><c:out value="${ped.estadoTexto}"/></span>
                        </div>

                        <p class="small mb-1">
                            <i class="bi bi-person me-1"></i><strong><c:out value="${ped.clienteNombre}"/></strong>
                            · DNI <c:out value="${ped.clienteDocumento}"/>
                            · Tel. <c:out value="${empty ped.clienteTelefono ? '—' : ped.clienteTelefono}"/>
                        </p>
                        <p class="small mb-3">
                            <i class="bi bi-credit-card me-1"></i><c:out value="${ped.metodoPagoTexto}"/>
                            <c:if test="${ped.estado eq 'PENDIENTE_PAGO' and not empty ped.fechaVencimientoTexto}">
                                · <strong>Vence el <c:out value="${ped.fechaVencimientoTexto}"/></strong>
                            </c:if>
                        </p>

                        <div class="table-responsive mb-3">
                            <table class="table table-sm align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>Producto</th>
                                        <th class="text-end">Precio</th>
                                        <th class="text-end">Cant.</th>
                                        <th class="text-end">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="d" items="${ped.detalles}">
                                        <tr>
                                            <td><c:out value="${d.nombreProducto}"/></td>
                                            <td class="text-end">S/ <fmt:formatNumber value="${d.precioUnitario}" pattern="#,##0.00"/></td>
                                            <td class="text-end">${d.cantidad}</td>
                                            <td class="text-end">S/ <fmt:formatNumber value="${d.subtotal}" pattern="#,##0.00"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <th colspan="3" class="text-end">Total</th>
                                        <th class="text-end">S/ <fmt:formatNumber value="${ped.total}" pattern="#,##0.00"/></th>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>

                        <%-- ===== Acciones según el estado ===== --%>
                        <div class="d-flex flex-wrap gap-3 align-items-center">

                            <c:if test="${ped.estado eq 'PENDIENTE_PAGO'}">
                                <form method="post" action="${ctx}/interno/pedidos"
                                      class="form-pago d-flex flex-wrap gap-2 align-items-center m-0">
                                    ${camposComunes}
                                    <input type="hidden" name="accion" value="pagar">
                                    <input type="hidden" name="idPedido" value="${ped.idPedido}">
                                    <select name="medio" class="form-select form-select-sm medio-pago" style="width: 190px;">
                                        <option value="EFECTIVO">Efectivo</option>
                                        <option value="POS">Tarjeta (POS)</option>
                                    </select>
                                    <input type="text" name="referencia" class="form-control form-control-sm referencia-pos"
                                           style="width: 260px;" maxlength="60" hidden disabled
                                           placeholder="N.° de operación del voucher POS">
                                    <button type="submit" class="btn btn-es btn-sm">
                                        <i class="bi bi-cash-coin"></i>Registrar pago
                                    </button>
                                </form>
                            </c:if>

                            <c:if test="${ped.estado eq 'PAGADO'}">
                                <form method="post" action="${ctx}/interno/pedidos" class="m-0">
                                    ${camposComunes}
                                    <input type="hidden" name="accion" value="listo">
                                    <input type="hidden" name="idPedido" value="${ped.idPedido}">
                                    <button type="submit" class="btn btn-es btn-sm">
                                        <i class="bi bi-box-seam"></i>Marcar como listo
                                    </button>
                                </form>
                            </c:if>

                            <c:if test="${ped.estado eq 'LISTO_RECOGER'}">
                                <form method="post" action="${ctx}/interno/pedidos" class="d-flex flex-wrap gap-2 align-items-center m-0">
                                    ${camposComunes}
                                    <input type="hidden" name="accion" value="entregar">
                                    <input type="hidden" name="idPedido" value="${ped.idPedido}">
                                    <input type="text" name="codigo" class="form-control form-control-sm"
                                           style="width: 200px; text-transform: uppercase;" maxlength="12" required
                                           placeholder="Código de recojo del cliente">
                                    <button type="submit" class="btn btn-es btn-sm">
                                        <i class="bi bi-bag-check"></i>Entregar
                                    </button>
                                </form>
                            </c:if>

                            <c:if test="${ped.estado eq 'PENDIENTE_PAGO' or ped.estado eq 'PAGADO' or ped.estado eq 'LISTO_RECOGER'}">
                                <form method="post" action="${ctx}/interno/pedidos" class="m-0"
                                      onsubmit="return confirm('¿Cancelar este pedido? Se liberará el stock reservado.');">
                                    ${camposComunes}
                                    <input type="hidden" name="accion" value="cancelar">
                                    <input type="hidden" name="idPedido" value="${ped.idPedido}">
                                    <button type="submit" class="btn btn-outline-danger btn-sm">
                                        <i class="bi bi-x-circle"></i>Cancelar pedido
                                    </button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>
        <script>
            // El N.° de operación solo se pide (y es obligatorio) si el pago fue con tarjeta (POS)
            document.querySelectorAll('.form-pago').forEach(function (form) {
                var medio = form.querySelector('.medio-pago');
                var ref = form.querySelector('.referencia-pos');

                function actualizar() {
                    var pos = medio.value === 'POS';
                    ref.hidden = !pos;
                    ref.disabled = !pos;
                    ref.required = pos;
                    if (!pos) {
                        ref.value = '';
                    }
                }
                medio.addEventListener('change', function () {
                    actualizar();
                    if (medio.value === 'POS') {
                        ref.focus();
                    }
                });
                actualizar();
            });
        </script>
    </body>
</html>

