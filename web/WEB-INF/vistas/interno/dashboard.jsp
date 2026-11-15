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
        <title>Panel interno | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
            <jsp:param name="activa" value="dashboard"/>
        </jsp:include>

        <main>
            <div class="encabezado-pagina">
                <div class="container-fluid px-4">
                    <h1>Bienvenido, <c:out value="${sessionScope.usuario.nombres}"/></h1>
                    <p>Rol: <strong><c:out value="${sessionScope.usuario.rol}"/></strong></p>
                </div>
            </div>

            <div class="container-fluid px-4 pb-5">

                <c:if test="${not empty errorResumen}">
                    <div class="alert alert-warning"><i class="bi bi-exclamation-triangle me-1"></i><c:out value="${errorResumen}"/></div>
                </c:if>

                <c:if test="${not empty resumen}">

                    <%-- ===== Aviso: pagos Yape/Plin esperando verificación ===== --%>
                    <c:if test="${resumen.pedidosPorVerificar gt 0}">
                        <div class="alert alert-info d-flex flex-wrap align-items-center gap-2">
                            <i class="bi bi-bell"></i>
                            <span>
                                <c:choose>
                                    <c:when test="${resumen.pedidosPorVerificar eq 1}">
                                        Hay <strong>1 pedido</strong> con un pago Yape/Plin por verificar.
                                    </c:when>
                                    <c:otherwise>
                                        Hay <strong>${resumen.pedidosPorVerificar} pedidos</strong> con pagos Yape/Plin por verificar.
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <a class="btn btn-es btn-sm ms-auto" href="${ctx}/interno/pedidos?estado=PAGO_EN_REVISION">
                                Revisar ahora
                            </a>
                        </div>
                    </c:if>

                    <%-- ===== Indicadores del negocio ===== --%>
                    <p class="etiqueta">Resumen</p>
                    <div class="row g-3 mb-4">
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/ventas">
                                <i class="bi bi-receipt fs-3 text-secondary"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">S/ <fmt:formatNumber value="${resumen.ventasHoyTotal}" pattern="#,##0.00"/></div>
                                    <div class="small text-secondary">
                                        Ventas de hoy &middot; ${resumen.ventasHoyCantidad} ${resumen.ventasHoyCantidad eq 1 ? 'venta' : 'ventas'}
                                    </div>
                                </div>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/ventas">
                                <i class="bi bi-graph-up-arrow fs-3 text-secondary"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">S/ <fmt:formatNumber value="${resumen.ventasMesTotal}" pattern="#,##0.00"/></div>
                                    <div class="small text-secondary">
                                        Ventas del mes &middot; ${resumen.ventasMesCantidad} ${resumen.ventasMesCantidad eq 1 ? 'venta' : 'ventas'}
                                    </div>
                                </div>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/productos?stock=bajo">
                                <i class="bi bi-exclamation-triangle fs-3 text-warning"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">${resumen.stockBajo}</div>
                                    <div class="small text-secondary">Productos con stock bajo</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/productos?stock=agotado">
                                <i class="bi bi-x-octagon fs-3 text-danger"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">${resumen.agotados}</div>
                                    <div class="small text-secondary">Productos agotados</div>
                                </div>
                            </a>
                        </div>
                    </div>

                    <%-- ===== Pedidos en línea que esperan una acción ===== --%>
                    <p class="etiqueta">Pedidos en línea</p>
                    <div class="row g-3 mb-4">
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/pedidos?estado=PAGO_EN_REVISION">
                                <i class="bi bi-hourglass-split fs-3 ${resumen.pedidosPorVerificar gt 0 ? 'text-primary' : 'text-secondary'}"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">${resumen.pedidosPorVerificar}</div>
                                    <div class="small text-secondary">Por verificar pago (Yape/Plin)</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/pedidos?estado=PENDIENTE_PAGO">
                                <i class="bi bi-cash-coin fs-3 text-secondary"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">${resumen.pedidosPorCobrar}</div>
                                    <div class="small text-secondary">Por cobrar en tienda</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/pedidos?estado=PAGADO">
                                <i class="bi bi-box-seam fs-3 text-secondary"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">${resumen.pedidosPorPreparar}</div>
                                    <div class="small text-secondary">Por preparar</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-3">
                            <a class="panel-es d-flex align-items-center gap-3 text-decoration-none h-100" href="${ctx}/interno/pedidos?estado=LISTO_RECOGER">
                                <i class="bi bi-bag-check fs-3 text-success"></i>
                                <div>
                                    <div class="fs-4 fw-bold lh-1 text-dark">${resumen.pedidosPorEntregar}</div>
                                    <div class="small text-secondary">Listos para entregar</div>
                                </div>
                            </a>
                        </div>
                    </div>
                </c:if>

                <p class="etiqueta">Módulos del sistema</p>

                <div class="row g-3">

                    <%-- ===== Módulos para todo el personal (administrador y vendedor) ===== --%>
                    <div class="col-12 col-sm-6 col-xl-4">
                        <a class="modulo" href="${ctx}/interno/productos">
                            <i class="bi bi-box-seam"></i>
                            <h2>Productos</h2>
                            <p>Inventario, precios y stock.</p>
                        </a>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-4">
                        <a class="modulo" href="${ctx}/interno/ventas">
                            <i class="bi bi-receipt"></i>
                            <h2>Ventas</h2>
                            <p>Registro de ventas presenciales.</p>
                        </a>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-4">
                        <a class="modulo" href="${ctx}/interno/pedidos">
                            <i class="bi bi-bag-check"></i>
                            <h2>Pedidos en línea</h2>
                            <p>Verificar pagos, preparar y entregar pedidos.</p>
                        </a>
                    </div>

                    <%-- ===== Módulos solo para el administrador ===== --%>
                    <c:if test="${sessionScope.usuario.administrador}">
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/categorias">
                                <i class="bi bi-tags"></i>
                                <h2>Categorías</h2>
                                <p>Clasificación de los productos.</p>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/proveedores">
                                <i class="bi bi-truck"></i>
                                <h2>Proveedores</h2>
                                <p>Datos de contacto de los proveedores.</p>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/compras">
                                <i class="bi bi-cart-plus"></i>
                                <h2>Compras</h2>
                                <p>Entradas de mercadería al almacén.</p>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/reportes">
                                <i class="bi bi-bar-chart-line"></i>
                                <h2>Reportes</h2>
                                <p>Ventas, ganancias y stock crítico.</p>
                                <span class="modulo-estado">Próximamente</span>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/usuarios">
                                <i class="bi bi-people"></i>
                                <h2>Usuarios</h2>
                                <p>Cuentas y roles del personal.</p>
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>
    </body>
</html>
