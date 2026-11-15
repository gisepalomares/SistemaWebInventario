<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Inicio | JD&amp;A Electro Speed</title>
        <jsp:include page="/WEB-INF/vistas/comunes/recursos.jsp"/>
        <link href="${ctx}/css/inicio.css" rel="stylesheet">
    </head>
    <body>

        <jsp:include page="/WEB-INF/vistas/comunes/navbar.jsp">
            <jsp:param name="activa" value="inicio"/>
        </jsp:include>

        <main>

            <%-- PORTADA: CARRUSEL PRINCIPAL --%>
            <section class="inicio-hero">

                <div id="carruselInicio" class="carrusel-inicio">

                    <%-- DIAPOSITIVA 1: PRESENTACIÓN --%>
                    <div class="hero-slide activo hero-slide-principal">

                        <div class="container">
                            <div class="row align-items-center">

                                <div class="col-12 col-lg-7">

                                    <div class="hero-texto">

                                        <p class="etiqueta">Repuestos vehiculares</p>

                                        <h1 class="inicio-titulo">
                                            ¡Tu vehículo en buenas manos,
                                            <span>tus repuestos al alcance!</span>
                                        </h1>

                                        <p class="inicio-descripcion">
                                            Encuentra lo que necesitas, realiza tu pedido
                                            en línea y recógelo en nuestra tienda de Comas.
                                            ¡Fácil, rápido y seguro!
                                        </p>

                                        <div class="inicio-botones">

                                            <a class="btn btn-es" href="${ctx}/catalogo">
                                                <i class="bi bi-grid"></i>
                                                Ver catálogo
                                            </a>

                                            <a class="btn btn-es-outline" href="${ctx}/registro">
                                                <i class="bi bi-person-plus"></i>
                                                Crear cuenta
                                            </a>

                                        </div>

                                        <p class="inicio-nota">
                                            <i class="bi bi-info-circle"></i>
                                            Los pedidos se retiran únicamente en tienda.
                                        </p>

                                    </div>

                                </div>

                                <%-- LADO DERECHO LIBRE PARA MOSTRAR LA IMAGEN --%>
                                <div class="col-lg-5 d-none d-lg-block">
                                    <div class="hero-espacio-vacio"></div>
                                </div>

                            </div>
                        </div>

                    </div>


                    <%-- DIAPOSITIVA 2: PEDIDOS EN LÍNEA --%>
                    <div class="hero-slide hero-slide-pedidos">

                        <div class="container">
                            <div class="row align-items-center">

                                <div class="col-12 col-lg-6">

                                    <div class="hero-texto">

                                        <p class="etiqueta">Compras fáciles y seguras</p>

                                        <h2 class="inicio-titulo">
                                            ¡Realiza tu pedido en línea
                                            <span>y recógelo en nuestra tienda!</span>
                                        </h2>

                                        <p class="inicio-descripcion">
                                            Consulta nuestro catálogo, reserva tus
                                            productos y realiza el seguimiento de tu pedido
                                            desde nuestra plataforma.
                                        </p>

                                        <div class="inicio-botones">

                                            <a class="btn btn-es" href="${ctx}/catalogo">
                                                <i class="bi bi-cart"></i>
                                                Comprar ahora
                                            </a>

                                            <a class="btn btn-es-outline" href="${ctx}/registro">
                                                <i class="bi bi-person-plus"></i>
                                                Crear cuenta
                                            </a>

                                        </div>

                                    </div>

                                </div>

                                <div class="col-12 col-lg-5 offset-lg-1">

                                    <div class="hero-panel">

                                        <p class="etiqueta">Pedidos en línea</p>

                                        <h2>Compre en línea y retire en tienda</h2>

                                        <div class="visual-linea"></div>

                                        <ul class="hero-lista">

                                            <li>
                                                <i class="bi bi-box-seam"></i>
                                                <div>
                                                    <strong>Disponibilidad actualizada</strong>
                                                    <span>
                                                        El catálogo muestra el stock real
                                                        de cada producto.
                                                    </span>
                                                </div>
                                            </li>

                                            <li>
                                                <i class="bi bi-clock-history"></i>
                                                <div>
                                                    <strong>Reserva de hasta 48 horas</strong>
                                                    <span>
                                                        Reserve su pedido y realice el pago
                                                        en tienda.
                                                    </span>
                                                </div>
                                            </li>

                                            <li>
                                                <i class="bi bi-upc-scan"></i>
                                                <div>
                                                    <strong>Retiro con código</strong>
                                                    <span>
                                                        Presente su código de recojo para
                                                        retirar su pedido.
                                                    </span>
                                                </div>
                                            </li>

                                        </ul>

                                    </div>

                                </div>

                            </div>
                        </div>

                    </div>


                    <%-- CONTROLES DEL CARRUSEL --%>
                    <button class="hero-flecha hero-anterior"
                            type="button"
                            aria-label="Diapositiva anterior">
                        <i class="bi bi-chevron-left"></i>
                    </button>

                    <button class="hero-flecha hero-siguiente"
                            type="button"
                            aria-label="Diapositiva siguiente">
                        <i class="bi bi-chevron-right"></i>
                    </button>

                    <%-- INDICADORES --%>
                    <div class="hero-indicadores">

                        <button class="hero-punto activo"
                                type="button"
                                data-slide="0"
                                aria-label="Ir a la primera diapositiva">
                        </button>

                        <button class="hero-punto"
                                type="button"
                                data-slide="1"
                                aria-label="Ir a la segunda diapositiva">
                        </button>

                    </div>

                </div>

            </section>



            <%-- Barra de marcas --%>                         
            <section class="seccion-marcas container">
                <div class="barra-anuncio">
                    <div class="contenido-marcas">
                        <div class="titulo-marcas">
                            TRABAJAMOS CON LAS MEJORES MARCAS
                        </div>
                        <div class="contenedor-marcas">
                            <div class="ticker-marcas">
                                <img src="${ctx}/img/marcas/toyota.png" alt="Toyota">
                                <img src="${ctx}/img/marcas/vistony.png" alt="Vistony">
                                <img src="${ctx}/img/marcas/hyundai.png" alt="hyundai">
                                <img src="${ctx}/img/marcas/nissan.png" alt="nissan">
                                <img src="${ctx}/img/marcas/mobilsuper.png" alt="mobil super">
                                <img src="${ctx}/img/marcas/bosch.png" alt="bosch">
                                <img src="${ctx}/img/marcas/volkswagen.png" alt="volkswagen">
                                <img src="${ctx}/img/marcas/Suzuki.png" alt="Suzuki">
                                <img src="${ctx}/img/marcas/shell-helix.png" alt="shell-helix">
                                <img src="${ctx}/img/marcas/castrol.png" alt="castrol">

                                <!-- Repetimos los logos para conseguir el efecto infinito -->
                                <img src="${ctx}/img/marcas/toyota.png" alt="Toyota">
                                <img src="${ctx}/img/marcas/vistony.png" alt="Vistony">
                                <img src="${ctx}/img/marcas/hyundai.png" alt="hyundai">
                                <img src="${ctx}/img/marcas/nissan.png" alt="nissan">
                                <img src="${ctx}/img/marcas/mobilsuper.png" alt="mobil super">
                                <img src="${ctx}/img/marcas/bosch.png" alt="bosch">
                                <img src="${ctx}/img/marcas/volkswagen.png" alt="volkswagen">
                                <img src="${ctx}/img/marcas/Suzuki.png" alt="Suzuki">
                                <img src="${ctx}/img/marcas/shell-helix.png" alt="shell-helix">
                                <img src="${ctx}/img/marcas/castrol.png" alt="castrol">
                            </div>
                        </div>
                    </div>
                </div>
            </section>
            <%-- CATEGORÍAS (cargadas desde la base de datos) --%>
            <section class="seccion-categorias container">

                <div class="encabezado-seccion">
                    <div>
                        <p class="etiqueta">Catálogo</p>
                        <h2>Categorías de productos</h2>
                        <p>Seleccione una categoría para consultar los productos disponibles.</p>
                    </div>
                    <a class="enlace-catalogo" href="${ctx}/catalogo">Ver todo el catálogo &rarr;</a>
                </div>

                <c:if test="${empty categorias}">
                    <div class="alert alert-secondary">
                        Las categorías no están disponibles en este momento. Puede consultar el
                        <a href="${ctx}/catalogo">catálogo completo</a>.
                    </div>
                </c:if>

                <div class="row g-3">
                    <c:forEach var="cat" items="${categorias}">
                        <c:choose>
                            <c:when test="${cat.nombre eq 'Filtros'}"><c:set var="icono" value="bi-funnel"/></c:when>
                            <c:when test="${cat.nombre eq 'Frenos'}"><c:set var="icono" value="bi-record-circle"/></c:when>
                            <c:when test="${cat.nombre eq 'Baterías'}"><c:set var="icono" value="bi-battery-charging"/></c:when>
                            <c:when test="${cat.nombre eq 'Lubricantes'}"><c:set var="icono" value="bi-droplet"/></c:when>
                            <c:when test="${cat.nombre eq 'Motor'}"><c:set var="icono" value="bi-gear-wide-connected"/></c:when>
                            <c:when test="${cat.nombre eq 'Eléctrico'}"><c:set var="icono" value="bi-lightning-charge"/></c:when>
                            <c:otherwise><c:set var="icono" value="bi-wrench-adjustable"/></c:otherwise>
                        </c:choose>

                        <div class="col-12 col-sm-6 col-lg-4">
                            <a class="categoria-card"
                               href="${ctx}/catalogo?categoria=${cat.idCategoria}">

                                <div class="categoria-imagen">
                                    <c:choose>
                                        <c:when test="${cat.nombre eq 'Baterías'}">
                                            <c:set var="imagen" value="baterias.jpg"/>
                                        </c:when>
                                        <c:when test="${cat.nombre eq 'Eléctrico'}">
                                            <c:set var="imagen" value="electrico.jpg"/>
                                        </c:when>
                                        <c:when test="${cat.nombre eq 'Filtros'}">
                                            <c:set var="imagen" value="filtros.jpg"/>
                                        </c:when>
                                        <c:when test="${cat.nombre eq 'Frenos'}">
                                            <c:set var="imagen" value="frenos.jpg"/>
                                        </c:when>
                                        <c:when test="${cat.nombre eq 'Lubricantes'}">
                                            <c:set var="imagen" value="lubricantes.jpg"/>
                                        </c:when>
                                        <c:when test="${cat.nombre eq 'Motor'}">
                                            <c:set var="imagen" value="motor.jpg"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="imagen" value="default.jpg"/>
                                        </c:otherwise>
                                    </c:choose>

                                    <img src="${ctx}/img/categorias/${imagen}"
                                         alt="<c:out value='${cat.nombre}'/>"
                                         loading="lazy">

                                </div>
                                <div class="categoria-contenido">
                                    <h3><c:out value="${cat.nombre}"/></h3>
                                    <p><c:out value="${cat.descripcion}"/></p>
                                    <span class="ver">Ver productos &rarr;</span>
                                </div>

                            </a>
                        </div>

                    </c:forEach>
                </div>
            </section>

            <%-- PROCESO DE COMPRA --%>
            <section class="seccion-proceso">
                <div class="container">
                    <div class="encabezado-seccion">
                        <div>
                            <p class="etiqueta">Proceso de compra</p>
                            <h2>Cómo realizar su pedido</h2>
                        </div>
                    </div>

                    <div class="row g-4">
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">01</span>
                                <h3>Consulte el catálogo</h3>
                                <p>Busque el repuesto por nombre, código o categoría y verifique su disponibilidad.</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">02</span>
                                <h3>Registre su pedido</h3>
                                <p>Inicie sesión, agregue los productos al carrito y confirme su pedido.</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">03</span>
                                <h3>Elija la forma de pago</h3>
                                <p>Pague en línea o reserve su pedido para pagar en tienda, con una vigencia de hasta 48 horas.</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="paso">
                                <span class="paso-numero">04</span>
                                <h3>Retire en tienda</h3>
                                <p>Presente su código de recojo en nuestra tienda de Comas para recibir su pedido.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

        </main>

        <jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>

        <%-- CARRUSEL DE LA PORTADA--%>
        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const carrusel = document.getElementById("carruselInicio");
                if (!carrusel)
                    return;
                const diapositivas = carrusel.querySelectorAll(".hero-slide");
                const puntos = carrusel.querySelectorAll(".hero-punto");
                const anterior = carrusel.querySelector(".hero-anterior");
                const siguiente = carrusel.querySelector(".hero-siguiente");
                let indiceActual = 0;
                let intervalo;
                const tiempoCambio = 6000;
                function mostrarDiapositiva(indice) {
                    diapositivas[indiceActual].classList.remove("activo");
                    puntos[indiceActual].classList.remove("activo");
                    indiceActual = (indice + diapositivas.length) % diapositivas.length;
                    diapositivas[indiceActual].classList.add("activo");
                    puntos[indiceActual].classList.add("activo");
                }
                function reiniciarAutomatico() {

                    clearInterval(intervalo);

                    intervalo = setInterval(function () {
                        mostrarDiapositiva(indiceActual + 1);
                    }, tiempoCambio);

                }
                anterior.addEventListener("click", function () {

                    mostrarDiapositiva(indiceActual - 1);
                    reiniciarAutomatico();
                });

                siguiente.addEventListener("click", function () {

                    mostrarDiapositiva(indiceActual + 1);
                    reiniciarAutomatico();

                });

                puntos.forEach(function (punto) {
                    punto.addEventListener("click", function () {
                        const indice = Number(this.dataset.slide);
                        mostrarDiapositiva(indice);
                        reiniciarAutomatico();

                    });

                });

                // Pausar cuando el usuario coloca el cursor sobre el carrusel
                carrusel.addEventListener("mouseenter", function () {
                    clearInterval(intervalo);
                });
                carrusel.addEventListener("mouseleave", function () {
                    reiniciarAutomatico();
                });
                // Iniciar carrusel automático
                reiniciarAutomatico();
            });
        </script>
    </body>
</html>
