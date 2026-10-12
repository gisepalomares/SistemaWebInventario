<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
                <p class="etiqueta">Módulos del sistema</p>

                <div class="row g-3">

                    <%-- ===== Módulos para todo el personal (administrador y vendedor) ===== --%>
                    <div class="col-12 col-sm-6 col-xl-4">
                        <a class="modulo" href="${ctx}/interno/productos">
                            <i class="bi bi-box-seam"></i>
                            <h2>Productos</h2>
                            <p>Inventario, precios y stock.</p>
                            <span class="modulo-estado">En desarrollo</span>
                        </a>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-4">
                        <a class="modulo" href="${ctx}/interno/ventas">
                            <i class="bi bi-receipt"></i>
                            <h2>Ventas</h2>
                            <p>Registro de ventas presenciales.</p>
                            <span class="modulo-estado">En desarrollo</span>
                        </a>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-4">
                        <a class="modulo" href="${ctx}/interno/pedidos">
                            <i class="bi bi-bag-check"></i>
                            <h2>Pedidos en línea</h2>
                            <p>Preparar, cobrar y entregar pedidos.</p>
                            <span class="modulo-estado">En desarrollo</span>
                        </a>
                    </div>

                    <%-- ===== Módulos solo para el administrador ===== --%>
                    <c:if test="${sessionScope.usuario.administrador}">
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/categorias">
                                <i class="bi bi-tags"></i>
                                <h2>Categorías</h2>
                                <p>Clasificación de los productos.</p>
                                <span class="modulo-estado">En desarrollo</span>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/proveedores">
                                <i class="bi bi-truck"></i>
                                <h2>Proveedores</h2>
                                <p>Datos de contacto de los proveedores.</p>
                                <span class="modulo-estado">En desarrollo</span>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/compras">
                                <i class="bi bi-cart-plus"></i>
                                <h2>Compras</h2>
                                <p>Entradas de mercadería al almacén.</p>
                                <span class="modulo-estado">En desarrollo</span>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/reportes">
                                <i class="bi bi-bar-chart-line"></i>
                                <h2>Reportes</h2>
                                <p>Ventas, ganancias y stock crítico.</p>
                                <span class="modulo-estado">En desarrollo</span>
                            </a>
                        </div>
                        <div class="col-12 col-sm-6 col-xl-4">
                            <a class="modulo" href="${ctx}/interno/usuarios">
                                <i class="bi bi-people"></i>
                                <h2>Usuarios</h2>
                                <p>Cuentas y roles del personal.</p>
                                <span class="modulo-estado modulo-listo">Disponible</span>
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>
    </body>
</html>
