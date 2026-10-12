<%-- 
    Document   : mis-pedidos
    Created on : 11 oct. 2026, 7:19:57 p. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<fmt:setLocale value="es_PE"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Mis pedidos | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="pedidos"/>
        </jsp:include>

        <main>
            <section class="encabezado-pagina">
                <div class="container">
                    <p class="etiqueta">Tienda en línea</p>
                    <h1>Mis pedidos</h1>
                    <p>Consulte el estado de sus pedidos y su código de recojo. Los pedidos se retiran únicamente en tienda.</p>
                </div>
            </section>

            <div class="container pb-5">

                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><c:out value="${error}"/></div>
                </c:if>

                <c:if test="${empty error and empty pedidos}">
                    <div class="panel-es text-center py-5">
                        <i class="bi bi-bag fs-1 text-muted"></i>
                        <p class="mt-3 mb-4">Aún no tiene pedidos.</p>
                        <a href="${ctx}/catalogo" class="btn btn-es">Ver catálogo</a>
                    </div>
                </c:if>

                <c:forEach var="ped" items="${pedidos}">

                    <c:choose>
                        <c:when test="${ped.estado eq 'PENDIENTE_PAGO'}"><c:set var="claseEstado" value="text-bg-warning"/></c:when>
                        <c:when test="${ped.estado eq 'PAGADO'}"><c:set var="claseEstado" value="text-bg-primary"/></c:when>
                        <c:when test="${ped.estado eq 'LISTO_RECOGER'}"><c:set var="claseEstado" value="text-bg-success"/></c:when>
                        <c:when test="${ped.estado eq 'ENTREGADO'}"><c:set var="claseEstado" value="text-bg-secondary"/></c:when>
                        <c:otherwise><c:set var="claseEstado" value="text-bg-danger"/></c:otherwise>
                    </c:choose>

                    <div class="panel-es mb-4">
                        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
                            <div>
                                <p class="text-muted small mb-0">Pedido N.° ${ped.idPedido} · <c:out value="${ped.fechaPedidoTexto}"/></p>
                                <p class="h4 fw-bold mb-0">Código: <c:out value="${ped.codigoRecojo}"/></p>
                            </div>
                            <span class="badge fs-6 ${claseEstado}"><c:out value="${ped.estadoTexto}"/></span>
                        </div>

                        <p class="small mb-3">
                            <i class="bi bi-credit-card me-1"></i><c:out value="${ped.metodoPagoTexto}"/>
                            <c:if test="${ped.estado eq 'PENDIENTE_PAGO' and not empty ped.fechaVencimientoTexto}">
                                · <strong>Pague en tienda antes del <c:out value="${ped.fechaVencimientoTexto}"/></strong>
                                o el pedido se cancelará.
                            </c:if>
                            <c:if test="${ped.estado eq 'LISTO_RECOGER'}">
                                · <strong>Ya puede recogerlo en tienda presentando su código.</strong>
                            </c:if>
                        </p>

                        <div class="table-responsive">
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
                    </div>
                </c:forEach>

                <c:if test="${not empty pedidos}">
                    <p class="small text-muted">
                        Total de pedidos: ${fn:length(pedidos)}.
                        <a href="${ctx}/catalogo">Seguir comprando</a>
                    </p>
                </c:if>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
