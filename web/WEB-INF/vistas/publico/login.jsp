<%-- 
    Document   : login
    Created on : 11 oct. 2026, 2:44:00 a. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="esPersonal" value="${tipo eq 'personal'}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${esPersonal ? 'Acceso del personal' : 'Ingresar'} | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="login"/>
            <jsp:param name="modo" value="${esPersonal ? 'personal' : ''}"/>
        </jsp:include>

        <main class="acceso">
            <div class="container">
                <div class="acceso-caja">

                    <p class="etiqueta">${esPersonal ? 'Panel interno' : 'Tienda en línea'}</p>
                    <h1 class="acceso-titulo">${esPersonal ? 'Acceso del personal' : 'Iniciar sesión'}</h1>
                    <p class="acceso-texto">
                        ${esPersonal
                          ? 'Ingrese con la cuenta que le asignó el administrador.'
                          : 'Ingrese con su correo y contraseña.'}
                    </p>

                    <%-- Aviso de cuenta creada (viene de /registro) --%>
                    <c:if test="${not empty ok}">
                        <div class="alert alert-success" role="alert">
                            <i class="bi bi-check-circle me-1"></i><c:out value="${ok}"/>
                        </div>
                    </c:if>

                    <%-- Mensaje de error enviado por LoginServlet --%>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle me-1"></i><c:out value="${error}"/>
                        </div>
                    </c:if>

                    <form method="post" action="${ctx}/login">
                        <%-- Recuerda si vino por "Acceso del personal" --%>
                        <c:if test="${esPersonal}">
                            <input type="hidden" name="tipo" value="personal">
                        </c:if>

                        <div class="mb-3">
                            <label for="correo" class="form-label">Correo electrónico</label>
                            <input type="email" class="form-control" id="correo" name="correo"
                                   value="<c:out value='${correo}'/>" maxlength="120"
                                   autocomplete="username" required autofocus>
                        </div>

                        <div class="mb-4">
                            <label for="clave" class="form-label">Contraseña</label>
                            <input type="password" class="form-control" id="clave" name="clave"
                                   maxlength="100" autocomplete="current-password" required>
                        </div>

                        <button type="submit" class="btn btn-es w-100">
                            <i class="bi bi-box-arrow-in-right"></i>Ingresar
                        </button>
                    </form>

                    <p class="acceso-pie">
                        <c:choose>
                            <c:when test="${esPersonal}">
                                ¿Es cliente? <a href="${ctx}/login">Ingrese aquí</a>
                            </c:when>
                            <c:otherwise>
                                ¿No tiene cuenta? <a href="${ctx}/registro">Créela aquí</a>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
