<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Crear cuenta | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/acceso.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="registro"/>
        </jsp:include>

        <main class="acceso">
            <div class="container">
                <div class="acceso-caja acceso-caja-ancha">

                    <p class="etiqueta">Tienda en línea</p>
                    <h1 class="acceso-titulo">Crear cuenta</h1>
                    <p class="acceso-texto">Necesita una cuenta para hacer pedidos y recogerlos en tienda.</p>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle me-1"></i><c:out value="${error}"/>
                        </div>
                    </c:if>

                    <form method="post" action="${ctx}/registro" autocomplete="off">
                        <div class="mb-3">
                            <label for="nombres" class="form-label">Nombres y apellidos</label>
                            <input type="text" class="form-control" id="nombres" name="nombres"
                                   value="<c:out value='${nombres}'/>" maxlength="100" required autofocus>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-12 col-sm-6">
                                <label for="documento" class="form-label">DNI</label>
                                <input type="text" class="form-control" id="documento" name="documento"
                                       value="<c:out value='${documento}'/>" inputmode="numeric"
                                       pattern="\d{8}" maxlength="8" title="8 dígitos" required>
                            </div>
                            <div class="col-12 col-sm-6">
                                <label for="telefono" class="form-label">Teléfono <span class="text-secondary">(opcional)</span></label>
                                <input type="text" class="form-control" id="telefono" name="telefono"
                                       value="<c:out value='${telefono}'/>" inputmode="numeric"
                                       pattern="\d{9}" maxlength="9" title="9 dígitos">
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="correo" class="form-label">Correo electrónico</label>
                            <input type="email" class="form-control" id="correo" name="correo"
                                   value="<c:out value='${correo}'/>" maxlength="120" required>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-12 col-sm-6">
                                <label for="clave" class="form-label">Contraseña</label>
                                <input type="password" class="form-control" id="clave" name="clave"
                                       maxlength="100" minlength="8" autocomplete="new-password" required>
                                <div class="form-text">Mínimo 8 caracteres, con letras y números.</div>
                            </div>
                            <div class="col-12 col-sm-6">
                                <label for="clave2" class="form-label">Repetir contraseña</label>
                                <input type="password" class="form-control" id="clave2" name="clave2"
                                       maxlength="100" minlength="8" autocomplete="new-password" required>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-es w-100">
                            <i class="bi bi-person-plus"></i>Crear cuenta
                        </button>
                    </form>

                    <p class="acceso-pie">
                        ¿Ya tiene cuenta? <a href="${ctx}/login">Inicie sesión</a>
                    </p>
                </div>
            </div>
        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
    </body>
</html>
