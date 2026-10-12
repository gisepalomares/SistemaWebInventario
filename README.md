\# JD\&A Electro Speed – Sistema web de inventario y ventas



Sistema web de gestión de inventario y ventas con tienda en línea (pedidos para recojo en tienda)

para una tienda de repuestos vehiculares. Java EE (Servlets, JSP/JSTL, JDBC), arquitectura MVC

con patrones DAO, DTO y Façade.



\## Requisitos

\- JDK 24 (según la configuración del proyecto)

\- Apache Tomcat 9.x

\- MySQL 8.x

\- NetBeans (proyecto Ant de aplicación web)



\## Instalación

1\. Crear la base de datos importando `database/electro\_speed.sql`:

&#x20;  `mysql -u root -p < database/electro\_speed.sql`

2\. Revisar usuario y clave de MySQL en `src/java/util/ConexionBD.java`.

3\. Abrir la carpeta del proyecto en NetBeans, ejecutar \*Clean and Build\* y luego \*Run\* con Tomcat.

4\. Entrar a `http://localhost:8080/ElectroSpeed/`.



Las librerías (.jar) están en la carpeta `lib/`.



\## Usuarios de prueba

\- Administrador: admin@electrospeed.com / Admin123\*

\- Vendedor: vendedor@electrospeed.com / Vende123\*

\- Clientes: se registran desde la tienda en línea (`/registro`).



\## Módulos

Tienda en línea: catálogo, carrito, pedidos con reserva de stock y pago simulado.

Panel interno: usuarios, categorías, productos, pedidos en línea (pago, preparación, entrega y cancelación).

