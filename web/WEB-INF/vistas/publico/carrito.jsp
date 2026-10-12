<%-- 
    Document   : carrito
    Created on : 11 oct. 2026, 6:29:47 p. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="es_PE"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Carrito | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="carrito"/>
        </jsp:include>

        <main>
            <section class="encabezado-pagina">
                <div class="container">
                    <p class="etiqueta">Tienda en línea</p>
                    <h1>Mi carrito</h1>
                    <p>Revise sus productos. El stock se reserva recién al confirmar el pedido.</p>
                </div>
            </section>

            <div class="container pb-5">

                <c:if test="${not empty flashOk}">
                    <div class="alert alert-success"><c:out value="${flashOk}"/></div>
                </c:if>
                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger"><c:out value="${flashError}"/></div>
                </c:if>
                <c:forEach var="a" items="${avisos}">
                    <div class="alert alert-warning"><c:out value="${a}"/></div>
                </c:forEach>

                <c:choose>
                    <c:when test="${empty sessionScope.carrito or sessionScope.carrito.vacio}">
                        <div class="panel-es text-center py-5">
                            <i class="bi bi-cart-x fs-1 text-muted"></i>
                            <p class="mt-3 mb-4">Su carrito está vacío.</p>
                            <a href="${ctx}/catalogo" class="btn btn-es">Ver catálogo</a>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="panel-es mb-4">
                            <div class="table-responsive">
                                <table class="table align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>Producto</th>
                                            <th class="text-end">Precio</th>
                                            <th style="width: 190px;">Cantidad</th>
                                            <th class="text-end">Subtotal</th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="i" items="${sessionScope.carrito.items}">
                                            <tr>
                                                <td>
                                                    <strong><c:out value="${i.nombre}"/></strong><br>
                                                    <span class="producto-sku">Código: <c:out value="${i.codigoSku}"/></span>
                                                </td>
                                                <td class="text-end">S/ <fmt:formatNumber value="${i.precioUnitario}" pattern="#,##0.00"/></td>
                                                <td>
                                                    <form method="post" action="${ctx}/carrito" class="d-flex gap-2">
                                                        <input type="hidden" name="csrf" value="${csrf}">
                                                        <input type="hidden" name="accion" value="actualizar">
                                                        <input type="hidden" name="idProducto" value="${i.idProducto}">
                                                        <input type="number" name="cantidad" value="${i.cantidad}"
                                                               min="1" max="${i.stockDisponible}" class="form-control form-control-sm"
                                                               aria-label="Cantidad">
                                                        <button type="submit" class="btn btn-es-outline btn-sm">Actualizar</button>
                                                    </form>
                                                    <small class="text-muted">Disponible: ${i.stockDisponible}</small>
                                                </td>
                                                <td class="text-end">S/ <fmt:formatNumber value="${i.subtotal}" pattern="#,##0.00"/></td>
                                                <td class="text-end">
                                                    <form method="post" action="${ctx}/carrito" class="m-0">
                                                        <input type="hidden" name="csrf" value="${csrf}">
                                                        <input type="hidden" name="accion" value="quitar">
                                                        <input type="hidden" name="idProducto" value="${i.idProducto}">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm" title="Quitar">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <th colspan="3" class="text-end">Total</th>
                                            <th class="text-end">S/ <fmt:formatNumber value="${sessionScope.carrito.total}" pattern="#,##0.00"/></th>
                                            <th></th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 mb-4">
                            <a href="${ctx}/catalogo" class="btn btn-es-outline">
                                <i class="bi bi-arrow-left"></i>Seguir comprando
                            </a>
                            <form method="post" action="${ctx}/carrito" class="m-0">
                                <input type="hidden" name="csrf" value="${csrf}">
                                <input type="hidden" name="accion" value="vaciar">
                                <button type="submit" class="btn btn-outline-danger">Vaciar carrito</button>
                            </form>
                        </div>

                        <form method="post" action="${ctx}/pedido/confirmar" class="panel-es">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <h2 class="h5 mb-3">Forma de pago</h2>

                            <div class="form-check mb-2">
                                <input class="form-check-input" type="radio" name="metodoPago" id="pagoTienda"
                                       value="EN_TIENDA" checked>
                                <label class="form-check-label" for="pagoTienda">
                                    <strong>Pagar en tienda</strong>: reservamos su pedido por 48 horas.
                                </label>
                            </div>
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="radio" name="metodoPago" id="pagoLinea"
                                       value="EN_LINEA_SIMULADO">
                                <label class="form-check-label" for="pagoLinea">
                                    <strong>Pagar en línea</strong> (simulado).
                                </label>
                            </div>

                            <div class="mb-3">
                                <label for="resultadoSimulado" class="form-label small text-muted">
                                    Resultado del pago simulado (solo demostración; aplica al pago en línea)
                                </label>
                                <select name="resultadoSimulado" id="resultadoSimulado" class="form-select"
                                        style="max-width: 260px;">
                                    <option value="APROBADO">Aprobado</option>
                                    <option value="RECHAZADO">Rechazado</option>
                                </select>
                            </div>

                            <p class="small text-muted">El retiro es únicamente en tienda, sin delivery.</p>
                            <button type="submit" class="btn btn-es">
                                <i class="bi bi-bag-check"></i>Confirmar pedido
                            </button>
                        </form>

                    </c:otherwise>

                </c:choose>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
