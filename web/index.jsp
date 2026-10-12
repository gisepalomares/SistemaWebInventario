<%-- 
    Document   : index
    Created on : 11 oct. 2026, 1:16:26 a. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- Página raíz: redirige a la portada (InicioServlet), que es quien carga las categorías --%>
<% response.sendRedirect(request.getContextPath() + "/inicio"); %>

