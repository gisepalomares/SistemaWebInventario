<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Inicio | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/inicio.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="inicio"/>
        </jsp:include>

        <main>

            <%-- PORTADA --%>
            <section class="inicio-hero">
                <div class="container">
                    <div class="row align-items-center g-4 g-lg-5">

                        <div class="col-12 col-lg-7">
                            <p class="etiqueta">Repuestos vehiculares</p>

                            <h1 class="inicio-titulo">
                                Repuestos para su vehículo,
                                <span>con disponibilidad actualizada.</span>
                            </h1>

                            <p class="inicio-descripcion">
                                Consulte el catálogo, registre su pedido en línea y retírelo
                                en nuestra tienda de Comas con su código de recojo.
                            </p>

                            <div class="inicio-botones">
                                <a class="btn btn-es" href="${ctx}/catalogo">
                                    <i class="bi bi-grid"></i>Ver catálogo
                                </a>
                                <a class="btn btn-es-outline" href="${ctx}/registro">
                                    <i class="bi bi-person-plus"></i>Crear cuenta
                                </a>
                            </div>

                            <p class="inicio-nota">
                                <i class="bi bi-info-circle"></i>
                                Los pedidos se retiran únicamente en tienda.
                            </p>
                        </div>

                        <div class="col-12 col-lg-5">
                            <div class="hero-panel">
                                <p class="etiqueta">Pedidos en línea</p>
                                <h2>Compre en línea y retire en tienda</h2>
                                <div class="visual-linea"></div>

                                <ul class="hero-lista">
                                    <li>
                                        <i class="bi bi-box-seam"></i>
                                        <div>
                                            <strong>Disponibilidad actualizada</strong>
                                            <span>El catálogo muestra el stock real de cada producto.</span>
                                        </div>
                                    </li>
                                    <li>
                                        <i class="bi bi-clock-history"></i>
                                        <div>
                                            <strong>Reserva de hasta 48 horas</strong>
                                            <span>Reserve su pedido y realice el pago en tienda.</span>
                                        </div>
                                    </li>
                                    <li>
                                        <i class="bi bi-upc-scan"></i>
                                        <div>
                                            <strong>Retiro con código</strong>
                                            <span>Presente su código de recojo para retirar su pedido.</span>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>

                    </div>
                </div>
            </section>

            <%-- CATEGORÍAS (cargadas desde la base de datos) --%>
            <section class="seccion-categorias container">

                <div class="encabezado-seccion">
                    <div>
                        <p class="etiqueta">Catálogo</p>
                        <h2>Categorías de productos</h2>
                        <p>Seleccione una categoría para consultar los productos disponibles.</p>
                    </div>
                    <a class="enlace-catalogo" href="${ctx}/catalogo">Ver todo el catálogo &rarr;</a>
                </div>

                <c:if test="${empty categorias}">
                    <div class="alert alert-secondary">
                        Las categorías no están disponibles en este momento. Puede consultar el
                        <a href="${ctx}/catalogo">catálogo completo</a>.
                    </div>
                </c:if>

                <div class="row g-3">
                    <c:forEach var="cat" items="${categorias}">
                        <c:choose>
                            <c:when test="${cat.nombre eq 'Filtros'}"><c:set var="icono" value="bi-funnel"/></c:when>
                            <c:when test="${cat.nombre eq 'Frenos'}"><c:set var="icono" value="bi-record-circle"/></c:when>
                            <c:when test="${cat.nombre eq 'Baterías'}"><c:set var="icono" value="bi-battery-charging"/></c:when>
                            <c:when test="${cat.nombre eq 'Lubricantes'}"><c:set var="icono" value="bi-droplet"/></c:when>
                            <c:when test="${cat.nombre eq 'Motor'}"><c:set var="icono" value="bi-gear-wide-connected"/></c:when>
                            <c:when test="${cat.nombre eq 'Eléctrico'}"><c:set var="icono" value="bi-lightning-charge"/></c:when>
                            <c:otherwise><c:set var="icono" value="bi-wrench-adjustable"/></c:otherwise>
                        </c:choose>
                        <div class="col-12 col-sm-6 col-lg-4">
                            <a class="categoria-card" href="${ctx}/catalogo?categoria=${cat.idCategoria}">
                                <div class="categoria-icono"><i class="bi ${icono}"></i></div>
                                <h3><c:out value="${cat.nombre}"/></h3>
                                <p><c:out value="${cat.descripcion}"/></p>
                                <span class="ver">Ver productos &rarr;</span>
                            </a>
                        </div>
                    </c:forEach>
                </div>
            </section>

            <%-- PROCESO DE COMPRA --%>
            <section class="seccion-proceso">
                <div class="container">
                    <div class="encabezado-seccion">
                        <div>
                            <p class="etiqueta">Proceso de compra</p>
                            <h2>Cómo realizar su pedido</h2>
                        </div>
                    </div>

                    <div class="row g-4">
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">01</span>
                                <h3>Consulte el catálogo</h3>
                                <p>Busque el repuesto por nombre, código o categoría y verifique su disponibilidad.</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">02</span>
                                <h3>Registre su pedido</h3>
                                <p>Inicie sesión, agregue los productos al carrito y confirme su pedido.</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">03</span>
                                <h3>Elija la forma de pago</h3>
                                <p>Pague en línea o reserve su pedido para pagar en tienda, con una vigencia de hasta 48 horas.</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">04</span>
                                <h3>Retire en tienda</h3>
                                <p>Presente su código de recojo en nuestra tienda de Comas para recibir su pedido.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
