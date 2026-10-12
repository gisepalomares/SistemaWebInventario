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
        <title>Catálogo | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="catalogo"/>
        </jsp:include>

        <main>
            <section class="encabezado-pagina">
                <div class="container">
                    <p class="etiqueta">Tienda en línea</p>
                    <h1>Catálogo de productos</h1>
                    <p>Consulte los repuestos disponibles. El stock mostrado corresponde a la disponibilidad real en tienda.</p>
                </div>
            </section>

            <div class="container pb-5">

                <%-- Filtros --%>
                <form method="get" action="${ctx}/catalogo" class="panel-es mb-4">
                    <div class="row g-2 align-items-center">
                        <div class="col-md-5">
                            <input type="text" name="q" value="<c:out value='${q}'/>" class="form-control"
                                   placeholder="Buscar por nombre o código (SKU)">
                        </div>
                        <div class="col-md-3">
                            <select name="categoria" class="form-select">
                                <option value="">Todas las categorías</option>
                                <c:forEach var="c" items="${categorias}">
                                    <option value="${c.idCategoria}" ${c.idCategoria == categoriaSel ? 'selected' : ''}>
                                        <c:out value="${c.nombre}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="soloStock" value="1" id="soloStock"
                                       ${soloStock ? 'checked' : ''}>
                                <label class="form-check-label" for="soloStock">Solo disponibles</label>
                            </div>
                        </div>
                        <div class="col-md-2 d-grid">
                            <button type="submit" class="btn btn-es"><i class="bi bi-search"></i>Buscar</button>
                        </div>
                    </div>
                </form>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><c:out value="${error}"/></div>
                </c:if>

                <c:if test="${not empty flashOk}">
                    <div class="alert alert-success"><c:out value="${flashOk}"/></div>
                </c:if>
                <c:if test="${not empty flashError}">
                    <div class="alert alert-warning"><c:out value="${flashError}"/></div>
                </c:if>
                    
                <c:if test="${empty error and empty productos}">
                    <div class="alert alert-secondary">No se encontraron productos con los criterios indicados.</div>
                </c:if>

                <c:if test="${not empty productos}">
                    <p class="text-muted small mb-3">${fn:length(productos)} producto(s) encontrado(s)</p>
                </c:if>

                <%-- Productos --%>
                <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4">
                    <c:forEach var="p" items="${productos}">
                        <div class="col">
                            <div class="card producto-card h-100">
                                <div class="card-body">
                                    <span class="producto-categoria"><c:out value="${p.categoria}"/></span>
                                    <h5 class="card-title"><c:out value="${p.nombre}"/></h5>
                                    <p class="producto-sku">Código: <c:out value="${p.codigoSku}"/></p>
                                    <p class="card-text small text-muted"><c:out value="${p.descripcion}"/></p>
                                </div>
                                <div class="card-footer bg-white border-0 pt-0 pb-3">
                                    <div class="d-flex justify-content-between align-items-center mb-3">
                                        <span class="producto-precio">
                                            S/ <fmt:formatNumber value="${p.precioVenta}" pattern="#,##0.00"/>
                                        </span>
                                        <c:choose>
                                            <c:when test="${p.agotado}">
                                                <span class="badge text-bg-danger">Agotado</span>
                                            </c:when>
                                            <c:when test="${p.stockBajo}">
                                                <span class="badge text-bg-warning">Stock limitado: ${p.stockDisponible}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge text-bg-success">Disponible: ${p.stockDisponible}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                                                        <c:choose>
                                        <c:when test="${p.agotado}">
                                            <button type="button" class="btn btn-es-outline w-100" disabled>
                                                <i class="bi bi-cart-plus"></i>Sin stock
                                            </button>
                                        </c:when>
                                        <c:when test="${empty sessionScope.usuario}">
                                            <a href="${ctx}/login" class="btn btn-es-outline w-100">
                                                <i class="bi bi-box-arrow-in-right"></i>Inicie sesión para comprar
                                            </a>
                                        </c:when>
                                        <c:when test="${sessionScope.usuario.personal}">
                                            <button type="button" class="btn btn-es-outline w-100" disabled
                                                    title="Los pedidos en línea son solo para clientes">
                                                <i class="bi bi-cart-plus"></i>Solo clientes
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <form method="post" action="${ctx}/carrito" class="d-flex gap-2 m-0">
                                                <input type="hidden" name="csrf" value="${csrf}">
                                                <input type="hidden" name="accion" value="agregar">
                                                <input type="hidden" name="idProducto" value="${p.idProducto}">
                                                <input type="hidden" name="volver" value="<c:out value='${pageContext.request.queryString}'/>">
                                                <input type="number" name="cantidad" value="1" min="1"
                                                       max="${p.stockDisponible}" class="form-control"
                                                       style="max-width: 5.5rem;" aria-label="Cantidad">
                                                <button type="submit" class="btn btn-es-outline flex-grow-1">
                                                    <i class="bi bi-cart-plus"></i>Agregar
                                                </button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
