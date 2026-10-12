<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%--
    Barra de navegación compartida.
    Uso:  <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
              <jsp:param name="activa" value="inicio"/>   (inicio | catalogo | carrito | pedidos | registro)
          </jsp:include>
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="navbar navbar-expand-lg navbar-dark navbar-es">
    <div class="container">

        <a class="navbar-brand" href="${ctx}/inicio">
            <i class="bi bi-gear-fill"></i>JD&amp;A Electro Speed
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#menuPrincipal" aria-controls="menuPrincipal"
                aria-expanded="false" aria-label="Mostrar menú">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="menuPrincipal">
            <ul class="navbar-nav me-auto ms-lg-4">
                <li class="nav-item">
                    <a class="nav-link ${param.activa eq 'inicio' ? 'active' : ''}" href="${ctx}/inicio">Inicio</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.activa eq 'catalogo' ? 'active' : ''}" href="${ctx}/catalogo">Catálogo</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.activa eq 'carrito' ? 'active' : ''}" href="${ctx}/carrito">Carrito<c:if test="${not empty sessionScope.carrito and sessionScope.carrito.cantidadTotal > 0}"><span class="badge rounded-pill text-bg-warning ms-1">${sessionScope.carrito.cantidadTotal}</span></c:if></a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.activa eq 'pedidos' ? 'active' : ''}" href="${ctx}/mis-pedidos">Mis pedidos</a>
                </li>
            </ul>

            <ul class="navbar-nav align-items-lg-center">
                <c:choose>

                    <%-- Con sesión iniciada: nombre + acceso al panel (si es personal) + cerrar sesión --%>
                    <c:when test="${not empty sessionScope.usuario}">
                        <c:if test="${sessionScope.usuario.personal}">
                            <li class="nav-item">
                                <a class="nav-link enlace-personal" href="${ctx}/interno/dashboard">
                                    <i class="bi bi-speedometer2"></i>Ir al panel
                                </a>
                            </li>
                        </c:if>
                        <li class="nav-item">
                            <span class="nav-link disabled-texto">
                                <i class="bi bi-person-circle me-1"></i><c:out value="${sessionScope.usuario.nombres}"/>
                            </span>
                        </li>
                        <li class="nav-item ms-lg-2">
                            <form method="post" action="${ctx}/logout" class="m-0">
                                <button type="submit" class="btn btn-ingresar">Cerrar sesión</button>
                            </form>
                        </li>
                    </c:when>

                    <%-- Pantalla "Acceso del personal": el personal NO se registra, así que no hay "Crear cuenta" --%>
                    <c:when test="${param.modo eq 'personal'}">
                        <li class="nav-item">
                            <a class="nav-link" href="${ctx}/login">
                                <i class="bi bi-person me-1"></i>Soy cliente
                            </a>
                        </li>
                    </c:when>

                    <%-- Visitante --%>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link ${param.activa eq 'registro' ? 'active' : ''}" href="${ctx}/registro">Crear cuenta</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link enlace-personal" href="${ctx}/login?tipo=personal">
                                <i class="bi bi-person-badge"></i>Acceso del personal
                            </a>
                        </li>
                        <li class="nav-item ms-lg-2">
                            <a class="btn btn-ingresar" href="${ctx}/login">Ingresar</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
