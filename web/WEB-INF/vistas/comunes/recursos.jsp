<%-- 
    Document   : recursos
    Created on : 11 oct. 2026, 1:21:34 a. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- Recursos comunes para el <head> de TODAS las páginas --%>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/estilos.css" rel="stylesheet">


<%-- Responsive: convierte las tablas en tarjetas en celular (el CSS está en estilos.css).
     Copia el texto de cada <th> en el atributo data-label de las celdas de su columna. --%>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('table').forEach(function (t) {
            if (t.classList.contains('tabla-fija')) {
                return;
            }
            var cabeceras = t.querySelectorAll('thead tr:last-child th');
            if (!cabeceras.length) {
                return;
            }
            var etiquetas = [];
            cabeceras.forEach(function (th) {
                var texto = th.textContent.replace(/\s+/g, ' ').trim();
                if (/^acciones?$/i.test(texto)) {
                    texto = '';
                }
                for (var i = 0; i < (th.colSpan || 1); i++) {
                    etiquetas.push(texto);
                }
            });
            t.querySelectorAll('tbody tr').forEach(function (tr) {
                var col = 0;
                Array.prototype.forEach.call(tr.children, function (td) {
                    var span = td.colSpan || 1;
                    td.setAttribute('data-label', span > 1 ? '' : (etiquetas[col] || ''));
                    col += span;
                });
            });
            t.classList.add('tabla-apilada');
        });
    });
</script>
