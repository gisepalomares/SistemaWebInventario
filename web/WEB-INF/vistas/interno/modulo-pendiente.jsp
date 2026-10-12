<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:out value="${moduloNombre}"/> | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="/WEB-INF/vistas/comunes/navbar-interno.jsp">
            <jsp:param name="activa" value="${moduloRuta}"/>
        </jsp:include>

        <main>
            <div class="encabezado-pagina">
                <div class="container-fluid px-4">
                    <h1><c:out value="${moduloNombre}"/></h1>
                    <p>Este módulo todavía no está desarrollado.</p>
                </div>
            </div>

            <div class="container-fluid px-4 pb-5">
                <div class="panel-es text-center py-5">
                    <i class="bi ${moduloIcono}" style="font-size:2.5rem;color:#6c757d"></i>
                    <h2 class="h5 mt-3">Módulo en construcción</h2>
                    <p class="text-secondary mb-4">
                        Aquí irá la pantalla de <strong><c:out value="${moduloNombre}"/></strong>.
                    </p>
                    <a class="btn btn-es-outline" href="${ctx}/interno/dashboard">
                        <i class="bi bi-arrow-left"></i>Volver al panel
                    </a>
                </div>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie-interno.jsp"/>
    </body>
</html>
