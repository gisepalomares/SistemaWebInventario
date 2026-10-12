<%-- 
    Document   : pie
    Created on : 11 oct. 2026, 1:20:40 a. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<%-- Pie de página compartido. Se incluye justo antes de </body> --%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<footer class="pie-es">
    <div class="container">
        <div class="row g-4">

            <div class="col-12 col-md-5">
                <p><strong>JD&amp;A Electro Speed</strong></p>
                <p>Venta de repuestos vehiculares con atención presencial y pedidos en línea para recojo en tienda.</p>
            </div>

            <div class="col-6 col-md-2">
                <h6>Tienda</h6>
                <a href="${ctx}/inicio">Inicio</a>
                <a href="${ctx}/catalogo">Catálogo</a>
                <a href="${ctx}/mis-pedidos">Mis pedidos</a>
            </div>

            <div class="col-6 col-md-2">
                <h6>Cuenta</h6>
                <a href="${ctx}/registro">Crear cuenta</a>
                <a href="${ctx}/login">Ingresar</a>
                <a href="${ctx}/login?tipo=personal">Acceso del personal</a>
            </div>

            <div class="col-12 col-md-3">
                <h6>Ubicación</h6>
                <p>Comas, Lima<br>Zona La Pascana</p>
                <p>Los pedidos se retiran únicamente en tienda.</p>
            </div>
        </div>

        <hr>
        <p class="copyright">&copy; 2026 JD&amp;A Electro Speed. Todos los derechos reservados.</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

