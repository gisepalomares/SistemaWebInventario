<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%--
    PLANTILLA DE LA ZONA INTERNA: barra superior + menú lateral izquierdo.
    Este archivo ABRE el diseño y pie-interno.jsp lo CIERRA, así que cada pantalla interna se arma así:

        <body>
            <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
                <jsp:param name="activa" value="usuarios"/>
            </jsp:include>

            <main> ...contenido de la pantalla (título, botones, tabla)... </main>

            <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>
        </body>

    Valores de "activa": dashboard | productos | categorias | proveedores | ventas | pedidos | compras | reportes | usuarios
    Para agregar un módulo al menú: copiar un <a class="menu-link"> y cambiar href, icono y el valor de "activa".
    Los enlaces dentro de <c:if test="${sessionScope.usuario.administrador}"> son solo del administrador.
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<%-- ================= Barra superior ================= --%>
<header class="topbar">
    <button class="btn topbar-toggle d-lg-none" type="button" data-bs-toggle="offcanvas"
            data-bs-target="#menuLateral" aria-controls="menuLateral" aria-label="Abrir menú">
        <i class="bi bi-list"></i>
    </button>

    <a class="topbar-marca" href="${ctx}/interno/dashboard">
        <i class="bi bi-gear-fill"></i>
        <span>JD&amp;A Electro Speed</span>
        <span class="topbar-sep d-none d-sm-inline">|</span>
        <span class="topbar-modulo d-none d-sm-inline">Módulo de Inventario</span>
    </a>

    <div class="dropdown ms-auto">
        <a class="topbar-usuario dropdown-toggle" href="#" role="button"
           data-bs-toggle="dropdown" aria-expanded="false">
            <i class="bi bi-person-circle"></i>
            <span class="d-none d-sm-inline"><c:out value="${sessionScope.usuario.nombres}"/></span>
        </a>
        <ul class="dropdown-menu dropdown-menu-end">
            <li><span class="dropdown-item-text small text-secondary">
                    <strong><c:out value="${sessionScope.usuario.nombres}"/></strong><br>
                    Rol: <c:out value="${sessionScope.usuario.rol}"/><br>
                    <c:out value="${sessionScope.usuario.correo}"/>
                </span></li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item" href="${ctx}/inicio" target="_blank" rel="noopener">
                    <i class="bi bi-shop me-1"></i>Ver tienda</a></li>
            <li>
                <form method="post" action="${ctx}/logout" class="m-0">
                    <button type="submit" class="dropdown-item">
                        <i class="bi bi-box-arrow-right me-1"></i>Cerrar sesión
                    </button>
                </form>
            </li>
        </ul>
    </div>
</header>

<%-- ================= Menú lateral + zona de contenido ================= --%>
<div class="app-interno">

    <div class="menu-col">
        <%-- En pantallas grandes se ve fijo a la izquierda; en celular se abre con el botón ☰ --%>
        <aside class="offcanvas-lg offcanvas-start" tabindex="-1" id="menuLateral" aria-label="Menú del sistema">
            <div class="offcanvas-header d-lg-none">
                <h2 class="offcanvas-title h6 mb-0">Menú</h2>
                <button type="button" class="btn-close" data-bs-dismiss="offcanvas" data-bs-target="#menuLateral" aria-label="Cerrar"></button>
            </div>
            <div class="offcanvas-body p-0">
                <nav class="menu-lateral">
                    <a class="menu-link ${param.activa eq 'dashboard' ? 'active' : ''}" href="${ctx}/interno/dashboard">
                        <i class="bi bi-speedometer2"></i>Dashboard</a>
                    <a class="menu-link ${param.activa eq 'productos' ? 'active' : ''}" href="${ctx}/interno/productos">
                        <i class="bi bi-box-seam"></i>Productos</a>

                    <c:if test="${sessionScope.usuario.administrador}">
                        <a class="menu-link ${param.activa eq 'categorias' ? 'active' : ''}" href="${ctx}/interno/categorias">
                            <i class="bi bi-tags"></i>Categorías</a>
                        <a class="menu-link ${param.activa eq 'proveedores' ? 'active' : ''}" href="${ctx}/interno/proveedores">
                            <i class="bi bi-truck"></i>Proveedores</a>
                    </c:if>

                    <a class="menu-link ${param.activa eq 'ventas' ? 'active' : ''}" href="${ctx}/interno/ventas">
                        <i class="bi bi-receipt"></i>Ventas</a>
                    <a class="menu-link ${param.activa eq 'pedidos' ? 'active' : ''}" href="${ctx}/interno/pedidos">
                        <i class="bi bi-bag-check"></i>Pedidos</a>

                    <c:if test="${sessionScope.usuario.administrador}">
                        <a class="menu-link ${param.activa eq 'compras' ? 'active' : ''}" href="${ctx}/interno/compras">
                            <i class="bi bi-cart-plus"></i>Compras</a>
                        <a class="menu-link ${param.activa eq 'reportes' ? 'active' : ''}" href="${ctx}/interno/reportes">
                            <i class="bi bi-bar-chart-line"></i>Reportes</a>
                        <a class="menu-link ${param.activa eq 'usuarios' ? 'active' : ''}" href="${ctx}/interno/usuarios">
                            <i class="bi bi-people"></i>Usuarios</a>
                    </c:if>
                </nav>
            </div>
        </aside>
    </div>

    <div class="app-contenido">
