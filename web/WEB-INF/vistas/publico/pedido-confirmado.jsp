<%-- 
    Document   : pedido-confirmado
    Created on : 11 oct. 2026, 6:45:03 p. m.
    Author     : USER
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="es_PE"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="p" value="${sessionScope.pedidoConfirmado}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Pedido registrado | JD&amp;A Electro Speed</title>
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
                    <h1>¡Pedido registrado!</h1>
                    <p>Presente este código en nuestra tienda para recoger su pedido.</p>
                </div>
            </section>

            <div class="container pb-5">

                <div class="panel-es text-center mb-4">
                    <p class="text-muted mb-1">Código de recojo</p>
                    <p class="display-5 fw-bold mb-2"><c:out value="${p.codigoRecojo}"/></p>
                    <span class="badge ${p.estado eq 'PAGADO' ? 'text-bg-success' : 'text-bg-warning'}">
                        <c:out value="${p.estadoTexto}"/>
                    </span>
                </div>

                <c:choose>
                    <c:when test="${p.metodoPago eq 'EN_TIENDA'}">
                        <div class="alert alert-warning">
                            <strong>Pague en tienda antes del <c:out value="${p.fechaVencimientoTexto}"/>.</strong>
                            Si no se registra el pago a tiempo, el pedido se cancelará y el stock se liberará automáticamente.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-success">
                            Pago en línea aprobado (simulado). Ya puede recoger su pedido cuando esté listo.
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="panel-es mb-4">
                    <p class="small text-muted mb-3">
                        Pedido N.° ${p.idPedido} · <c:out value="${p.fechaPedidoTexto}"/> ·
                        <c:out value="${p.metodoPagoTexto}"/>
                    </p>
                    <div class="table-responsive">
                        <table class="table align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Producto</th>
                                    <th class="text-end">Precio</th>
                                    <th class="text-end">Cant.</th>
                                    <th class="text-end">Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="d" items="${p.detalles}">
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
                                    <th class="text-end">S/ <fmt:formatNumber value="${p.total}" pattern="#,##0.00"/></th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                <p class="small text-muted">Los pedidos se retiran únicamente en tienda (no hay delivery).</p>
                <a href="${ctx}/mis-pedidos" class="btn btn-es">Ver mis pedidos</a>
                <a href="${ctx}/catalogo" class="btn btn-es-outline">Seguir comprando</a>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
