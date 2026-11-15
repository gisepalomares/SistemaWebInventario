<%-- 
    Document   : carrito
    Created on : 11 oct. 2026, 6:29:47 p. m.
    Author     : USER
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="es_PE"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- ===== Pago en línea con Yape / Plin: datos de la empresa (edítelos aquí) =====
     Los códigos QR son las imágenes web/img/pago/yape-qr.png y web/img/pago/plin-qr.png
     (reemplácelas por los QR reales de la empresa, con el mismo nombre).
     Opcional: si completa el titular o los números, se muestran bajo el QR por si el cliente no puede escanear. --%>
<c:set var="titularPago" value=""/>
<c:set var="numeroYape" value=""/>
<c:set var="numeroPlin" value=""/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Carrito | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="carrito"/>
        </jsp:include>

        <main>
            <section class="encabezado-pagina">
                <div class="container">
                    <p class="etiqueta">Tienda en línea</p>
                    <h1>Mi carrito</h1>
                    <p>Revise sus productos. El stock se reserva recién al confirmar el pedido.</p>
                </div>
            </section>

            <div class="container pb-5">

                <c:if test="${not empty flashOk}">
                    <div class="alert alert-success"><c:out value="${flashOk}"/></div>
                </c:if>
                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger"><c:out value="${flashError}"/></div>
                </c:if>
                <c:forEach var="a" items="${avisos}">
                    <div class="alert alert-warning"><c:out value="${a}"/></div>
                </c:forEach>

                <c:choose>
                    <c:when test="${empty sessionScope.carrito or sessionScope.carrito.vacio}">
                        <div class="panel-es text-center py-5">
                            <i class="bi bi-cart-x fs-1 text-muted"></i>
                            <p class="mt-3 mb-4">Su carrito está vacío.</p>
                            <a href="${ctx}/catalogo" class="btn btn-es">Ver catálogo</a>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="panel-es mb-4">
                            <div class="table-responsive">
                                <table class="table align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>Producto</th>
                                            <th class="text-end">Precio</th>
                                            <th style="width: 190px;">Cantidad</th>
                                            <th class="text-end">Subtotal</th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="i" items="${sessionScope.carrito.items}">
                                            <tr>
                                                <td>
                                                    <strong><c:out value="${i.nombre}"/></strong><br>
                                                    <span class="producto-sku">Código: <c:out value="${i.codigoSku}"/></span>
                                                </td>
                                                <td class="text-end">S/ <fmt:formatNumber value="${i.precioUnitario}" pattern="#,##0.00"/></td>
                                                <td>
                                                    <form method="post" action="${ctx}/carrito" class="d-flex gap-2">
                                                        <input type="hidden" name="csrf" value="${csrf}">
                                                        <input type="hidden" name="accion" value="actualizar">
                                                        <input type="hidden" name="idProducto" value="${i.idProducto}">
                                                        <input type="number" name="cantidad" value="${i.cantidad}"
                                                               min="1" max="${i.stockDisponible}" class="form-control form-control-sm"
                                                               aria-label="Cantidad">
                                                        <button type="submit" class="btn btn-es-outline btn-sm">Actualizar</button>
                                                    </form>
                                                    <small class="text-muted">Disponible: ${i.stockDisponible}</small>
                                                </td>
                                                <td class="text-end">S/ <fmt:formatNumber value="${i.subtotal}" pattern="#,##0.00"/></td>
                                                <td class="text-end">
                                                    <form method="post" action="${ctx}/carrito" class="m-0">
                                                        <input type="hidden" name="csrf" value="${csrf}">
                                                        <input type="hidden" name="accion" value="quitar">
                                                        <input type="hidden" name="idProducto" value="${i.idProducto}">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm" title="Quitar">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <th colspan="3" class="text-end">Total</th>
                                            <th class="text-end">S/ <fmt:formatNumber value="${sessionScope.carrito.total}" pattern="#,##0.00"/></th>
                                            <th></th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 mb-4">
                            <a href="${ctx}/catalogo" class="btn btn-es-outline">
                                <i class="bi bi-arrow-left"></i>Seguir comprando
                            </a>
                            <form method="post" action="${ctx}/carrito" class="m-0">
                                <input type="hidden" name="csrf" value="${csrf}">
                                <input type="hidden" name="accion" value="vaciar">
                                <button type="submit" class="btn btn-outline-danger">Vaciar carrito</button>
                            </form>
                        </div>

                        <form method="post" action="${ctx}/pedido/confirmar" enctype="multipart/form-data"
                              id="formPedido" class="panel-es">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <h2 class="h5 mb-3">Forma de pago</h2>

                            <div class="form-check mb-2">
                                <input class="form-check-input" type="radio" name="metodoPago" id="pagoTienda"
                                       value="EN_TIENDA" checked>
                                <label class="form-check-label" for="pagoTienda">
                                    <strong>Pagar en tienda</strong>: reservamos su pedido por 48 horas.
                                </label>
                            </div>
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="radio" name="metodoPago" id="pagoLinea"
                                       value="EN_LINEA_QR">
                                <label class="form-check-label" for="pagoLinea">
                                    <strong>Pagar en línea</strong> con Yape o Plin: escanee el QR y suba la captura de su pago.
                                </label>
                            </div>

                            <%-- Se muestra cuando ya se adjuntó la captura del pago --%>
                            <div id="resumenCaptura" class="d-none mb-3">
                                <div class="alert alert-success d-flex flex-wrap align-items-center gap-2 py-2 mb-0 small">
                                    <i class="bi bi-check-circle"></i>
                                    <span>Captura lista: <strong id="nombreCaptura"></strong></span>
                                    <button type="button" class="btn btn-link btn-sm p-0 ms-auto" id="btnCambiarCaptura">Cambiar captura</button>
                                </div>
                            </div>

                            <p class="small text-muted">El retiro es únicamente en tienda, sin delivery.</p>
                            <button type="submit" class="btn btn-es" id="btnConfirmar">
                                <i class="bi bi-bag-check"></i><span id="txtConfirmar">Confirmar pedido</span>
                            </button>
                        </form>

                    </c:otherwise>

                </c:choose>
            </div>
        </main>

        <c:if test="${not empty sessionScope.carrito and not sessionScope.carrito.vacio}">
            <fmt:formatNumber var="montoPago" value="${sessionScope.carrito.total}" pattern="#,##0.00"/>

            <%-- ===== Ventana 1: QR de Yape / Plin con el monto a pagar ===== --%>
            <div class="modal fade" id="modalQR" tabindex="-1" aria-labelledby="tituloQR" aria-hidden="true"
                 data-bs-backdrop="static">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 class="modal-title h5" id="tituloQR">Pague con Yape o Plin</h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body text-center">
                            <div class="btn-group mb-3" role="group" aria-label="Elija dónde pagar">
                                <input type="radio" class="btn-check" name="medioQR" id="medioYape" value="YAPE"
                                       form="formPedido" autocomplete="off" checked>
                                <label class="btn btn-es-outline" for="medioYape">Yape</label>
                                <input type="radio" class="btn-check" name="medioQR" id="medioPlin" value="PLIN"
                                       form="formPedido" autocomplete="off">
                                <label class="btn btn-es-outline" for="medioPlin">Plin</label>
                            </div>

                            <div id="qrYape">
                                <img src="${ctx}/img/pago/yape-qr.png" alt="Código QR de Yape" class="qr-pago">
                                <c:if test="${not empty numeroYape}">
                                    <p class="small text-muted mt-2 mb-0">O yapee al número <strong><c:out value="${numeroYape}"/></strong></p>
                                </c:if>
                            </div>
                            <div id="qrPlin" class="d-none">
                                <img src="${ctx}/img/pago/plin-qr.png" alt="Código QR de Plin" class="qr-pago">
                                <c:if test="${not empty numeroPlin}">
                                    <p class="small text-muted mt-2 mb-0">O pague por Plin al número <strong><c:out value="${numeroPlin}"/></strong></p>
                                </c:if>
                            </div>

                            <p class="text-muted small mt-3 mb-0">Monto a pagar</p>
                            <p class="monto-qr mb-2">S/ ${montoPago}</p>
                            <c:if test="${not empty titularPago}">
                                <p class="small mb-2">Titular: <strong><c:out value="${titularPago}"/></strong></p>
                            </c:if>
                            <p class="small text-muted mb-0">
                                Pague exactamente este monto y tome una captura de pantalla de la confirmación del pago.
                            </p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-es-outline" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-es" id="btnProcesar">Procesar</button>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== Ventana 2: subir la captura del pago ===== --%>
            <div class="modal fade" id="modalComprobante" tabindex="-1" aria-labelledby="tituloComprobante"
                 aria-hidden="true" data-bs-backdrop="static">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 class="modal-title h5" id="tituloComprobante">Suba la captura de su pago</h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body">
                            <p class="mb-3">
                                Adjunte la captura de pantalla de su pago con <strong id="medioElegido">Yape</strong>
                                por <strong>S/ ${montoPago}</strong>.
                            </p>
                            <label for="comprobante" class="form-label">Captura del pago</label>
                            <input type="file" class="form-control" id="comprobante" name="comprobante"
                                   form="formPedido" accept="image/jpeg,image/png,image/webp">
                            <div class="form-text">Imagen JPG, PNG o WEBP de hasta 5 MB.</div>
                            <div id="errorCaptura" class="text-danger small mt-2 d-none" role="alert"></div>
                            <div class="text-center">
                                <img id="vistaPrevia" class="img-fluid border mt-3 d-none vista-previa-captura"
                                     alt="Vista previa de la captura">
                            </div>
                            <p class="small text-muted mt-3 mb-0">
                                Un vendedor verificará que el pago llegó a la cuenta de la tienda y confirmará su pedido.
                            </p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-es-outline" id="btnVolverQR">Volver al QR</button>
                            <button type="submit" class="btn btn-es" id="btnEnviar" form="formPedido" disabled>
                                Enviar comprobante
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
        <c:if test="${not empty sessionScope.carrito and not sessionScope.carrito.vacio}">
            <script>
                // Pago en línea: al elegirlo se abre el QR; "Procesar" lo cierra y abre la subida de la captura.
                (function () {
                    var MAX_BYTES = 5 * 1024 * 1024;
                    var TIPOS = ['image/jpeg', 'image/png', 'image/webp'];

                    var form = document.getElementById('formPedido');
                    var archivo = document.getElementById('comprobante');
                    var btnConfirmar = document.getElementById('btnConfirmar');
                    var txtConfirmar = document.getElementById('txtConfirmar');
                    var resumen = document.getElementById('resumenCaptura');
                    var nombreCaptura = document.getElementById('nombreCaptura');
                    var elQR = document.getElementById('modalQR');
                    var elComp = document.getElementById('modalComprobante');
                    var modalQR = new bootstrap.Modal(elQR);
                    var modalComp = new bootstrap.Modal(elComp);
                    var btnEnviar = document.getElementById('btnEnviar');
                    var error = document.getElementById('errorCaptura');
                    var vista = document.getElementById('vistaPrevia');
                    var siguiente = null;      // ventana que se abre cuando termina de cerrarse la actual
                    var urlVista = null;

                    function enLinea() {
                        return form.querySelector('input[name="metodoPago"]:checked').value === 'EN_LINEA_QR';
                    }
                    function hayCaptura() {
                        return archivo.files.length > 0;
                    }
                    function esYape() {
                        return document.querySelector('input[name="medioQR"]:checked').value === 'YAPE';
                    }

                    function actualizarPantalla() {
                        document.getElementById('qrYape').classList.toggle('d-none', !esYape());
                        document.getElementById('qrPlin').classList.toggle('d-none', esYape());
                        document.getElementById('medioElegido').textContent = esYape() ? 'Yape' : 'Plin';
                        resumen.classList.toggle('d-none', !(enLinea() && hayCaptura()));
                        txtConfirmar.textContent = !enLinea() ? 'Confirmar pedido'
                                : (hayCaptura() ? 'Enviar pedido y comprobante' : 'Pagar con Yape o Plin');
                    }

                    // --- Elegir la forma de pago ---
                    form.querySelectorAll('input[name="metodoPago"]').forEach(function (r) {
                        r.addEventListener('change', function () {
                            actualizarPantalla();
                            if (enLinea() && !hayCaptura()) {
                                modalQR.show();
                            }
                        });
                    });
                    document.querySelectorAll('input[name="medioQR"]').forEach(function (r) {
                        r.addEventListener('change', actualizarPantalla);
                    });

                    // --- Pasar de la ventana del QR a la de la captura (y volver) ---
                    document.getElementById('btnProcesar').addEventListener('click', function () {
                        siguiente = 'comprobante';
                        modalQR.hide();
                    });
                    document.getElementById('btnVolverQR').addEventListener('click', function () {
                        siguiente = 'qr';
                        modalComp.hide();
                    });
                    elQR.addEventListener('hidden.bs.modal', function () {
                        if (siguiente === 'comprobante') {
                            siguiente = null;
                            modalComp.show();
                        }
                    });
                    elComp.addEventListener('hidden.bs.modal', function () {
                        if (siguiente === 'qr') {
                            siguiente = null;
                            modalQR.show();
                        }
                    });
                    document.getElementById('btnCambiarCaptura').addEventListener('click', function () {
                        modalComp.show();
                    });

                    // --- Validar la imagen elegida (el servidor vuelve a validarla) ---
                    archivo.addEventListener('change', function () {
                        error.classList.add('d-none');
                        vista.classList.add('d-none');
                        btnEnviar.disabled = true;
                        if (urlVista) {
                            URL.revokeObjectURL(urlVista);
                            urlVista = null;
                        }
                        var f = archivo.files[0];
                        if (f) {
                            var mensaje = null;
                            if (TIPOS.indexOf(f.type) === -1) {
                                mensaje = 'Suba una imagen JPG, PNG o WEBP.';
                            } else if (f.size > MAX_BYTES) {
                                mensaje = 'La imagen pesa más de 5 MB. Suba una más liviana.';
                            }
                            if (mensaje) {
                                archivo.value = '';
                                error.textContent = mensaje;
                                error.classList.remove('d-none');
                            } else {
                                urlVista = URL.createObjectURL(f);
                                vista.src = urlVista;
                                vista.classList.remove('d-none');
                                nombreCaptura.textContent = f.name;
                                btnEnviar.disabled = false;
                            }
                        }
                        actualizarPantalla();
                    });

                    // --- Enviar ---
                    form.addEventListener('submit', function (e) {
                        if (enLinea()) {
                            if (!hayCaptura()) {          // pagar en línea sin captura: se abre el QR
                                e.preventDefault();
                                modalQR.show();
                                return;
                            }
                        } else {
                            archivo.disabled = true;      // pagando en tienda no se envía ninguna imagen
                        }
                        btnConfirmar.disabled = true;     // evita doble clic
                        btnEnviar.disabled = true;
                    });
                    // Si el cliente vuelve con "atrás", los botones quedan activos de nuevo
                    window.addEventListener('pageshow', function () {
                        archivo.disabled = false;
                        btnConfirmar.disabled = false;
                        btnEnviar.disabled = !hayCaptura();
                        actualizarPantalla();
                    });

                    actualizarPantalla();
                })();
            </script>
        </c:if>
    </body>
</html>
