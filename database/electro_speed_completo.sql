-- =====================================================================
--  JD&A Electro Speed - Script ÚNICO de la base de datos (instalación desde cero)
--  Incluye el pago en línea con Yape / Plin (captura de pantalla).
--
--  Uso:  mysql -u root -p < electro_speed_completo.sql
--  NOTA: es para una base NUEVA. Si ya existe `electro_speed` con datos,
--        NO lo ejecute: use database/migracion_pago_yape_plin.sql.
-- =====================================================================
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS electro_speed CHARACTER SET utf8mb4;

USE electro_speed;
-- ---------------------------------------------------------------------
--  SEGURIDAD: roles y usuarios (Administrador, Vendedor, Cliente)
-- ---------------------------------------------------------------------
CREATE TABLE rol (
  id_rol      INT          NOT NULL AUTO_INCREMENT,
  nombre_rol  VARCHAR(30)  NOT NULL,
  PRIMARY KEY (id_rol),
  UNIQUE KEY uk_rol_nombre (nombre_rol)
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario  INT           NOT NULL AUTO_INCREMENT,
  nombres     VARCHAR(100)  NOT NULL,
  correo      VARCHAR(120)  NOT NULL,
  contrasena  VARCHAR(255)  NOT NULL COMMENT 'Hash de la contraseña (nunca texto plano)',
  id_rol      INT           NOT NULL,
  estado      TINYINT(1)    NOT NULL DEFAULT 1,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uk_usuario_correo (correo),
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id_cliente  INT           NOT NULL AUTO_INCREMENT,
  nombres     VARCHAR(100)  NOT NULL,
  documento   VARCHAR(15)   NULL,
  telefono    VARCHAR(20)   NULL,
  id_usuario  INT           NULL COMMENT 'NULL = cliente atendido solo de forma presencial',
  PRIMARY KEY (id_cliente),
  UNIQUE KEY uk_cliente_documento (documento),
  UNIQUE KEY uk_cliente_usuario (id_usuario),
  CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  INVENTARIO: categorías, proveedores y productos
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
  id_categoria  INT           NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(60)   NOT NULL,
  descripcion   VARCHAR(200)  NULL,
  PRIMARY KEY (id_categoria),
  UNIQUE KEY uk_categoria_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE proveedor (
  id_proveedor      INT           NOT NULL AUTO_INCREMENT,
  ruc               CHAR(11)      NOT NULL,
  razon_social      VARCHAR(120)  NOT NULL,
  nombre_comercial  VARCHAR(120)  NULL,
  direccion         VARCHAR(200)  NULL,
  persona_contacto  VARCHAR(100)  NULL,
  telefono          VARCHAR(20)   NULL,
  correo            VARCHAR(120)  NULL,
  PRIMARY KEY (id_proveedor),
  UNIQUE KEY uk_proveedor_ruc (ruc)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto      INT            NOT NULL AUTO_INCREMENT,
  codigo_sku       VARCHAR(20)    NOT NULL,
  nombre           VARCHAR(120)   NOT NULL,
  descripcion      VARCHAR(255)   NULL,
  precio_venta     DECIMAL(10,2)  NOT NULL,
  stock_actual     INT            NOT NULL DEFAULT 0,
  stock_reservado  INT            NOT NULL DEFAULT 0 COMMENT 'Reservado por pedidos en línea pendientes',
  stock_minimo     INT            NOT NULL DEFAULT 0,
  id_categoria     INT            NOT NULL,
  id_proveedor     INT            NULL,
  estado           TINYINT(1)     NOT NULL DEFAULT 1,
  PRIMARY KEY (id_producto),
  UNIQUE KEY uk_producto_sku (codigo_sku),
  KEY idx_producto_categoria (id_categoria),
  KEY idx_producto_proveedor (id_proveedor),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria),
  CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
  CONSTRAINT ck_producto_precio   CHECK (precio_venta >= 0),
  CONSTRAINT ck_producto_stock    CHECK (stock_actual >= 0 AND stock_reservado >= 0
                                         AND stock_reservado <= stock_actual AND stock_minimo >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  COMPRAS (entradas de inventario)
-- ---------------------------------------------------------------------
CREATE TABLE compra (
  id_compra     INT            NOT NULL AUTO_INCREMENT,
  fecha         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_proveedor  INT            NOT NULL,
  total         DECIMAL(12,2)  NOT NULL DEFAULT 0,
  estado        ENUM('REGISTRADA','ANULADA') NOT NULL DEFAULT 'REGISTRADA',
  PRIMARY KEY (id_compra),
  KEY idx_compra_proveedor (id_proveedor),
  CONSTRAINT fk_compra_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor)
) ENGINE=InnoDB;

CREATE TABLE detalle_compra (
  id_detalle_compra  INT            NOT NULL AUTO_INCREMENT,
  id_compra          INT            NOT NULL,
  id_producto        INT            NOT NULL,
  cantidad           INT            NOT NULL,
  precio_unitario    DECIMAL(10,2)  NOT NULL,
  PRIMARY KEY (id_detalle_compra),
  KEY idx_dcompra_compra (id_compra),
  KEY idx_dcompra_producto (id_producto),
  CONSTRAINT fk_dcompra_compra   FOREIGN KEY (id_compra)   REFERENCES compra (id_compra),
  CONSTRAINT fk_dcompra_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
  CONSTRAINT ck_dcompra_cantidad CHECK (cantidad > 0 AND precio_unitario >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  PEDIDOS EN LÍNEA (zona pública) y PAGOS
-- ---------------------------------------------------------------------
CREATE TABLE pedido (
  id_pedido          INT            NOT NULL AUTO_INCREMENT,
  fecha_pedido       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- PAGO_EN_REVISION: el cliente ya subió su captura de Yape/Plin y el vendedor debe verificarla
  estado             ENUM('PENDIENTE_PAGO','PAGO_EN_REVISION','PAGADO','LISTO_RECOGER','ENTREGADO','VENCIDO','CANCELADO')
                     NOT NULL DEFAULT 'PENDIENTE_PAGO',
  codigo_recojo      VARCHAR(12)    NOT NULL,
  fecha_vencimiento  DATETIME       NULL COMMENT 'Solo aplica a pedidos con pago en tienda',
  -- EN_LINEA_QR: pago con Yape/Plin. EN_LINEA_SIMULADO solo existe en pedidos antiguos.
  metodo_pago        ENUM('EN_LINEA_SIMULADO','EN_TIENDA','EN_LINEA_QR') NOT NULL,
  total              DECIMAL(12,2)  NOT NULL DEFAULT 0,
  id_cliente         INT            NOT NULL,
  PRIMARY KEY (id_pedido),
  UNIQUE KEY uk_pedido_codigo (codigo_recojo),
  KEY idx_pedido_cliente (id_cliente),
  KEY idx_pedido_estado_venc (estado, fecha_vencimiento),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido (
  id_detalle_pedido  INT            NOT NULL AUTO_INCREMENT,
  id_pedido          INT            NOT NULL,
  id_producto        INT            NOT NULL,
  cantidad           INT            NOT NULL,
  precio_unitario    DECIMAL(10,2)  NOT NULL,
  PRIMARY KEY (id_detalle_pedido),
  KEY idx_dpedido_pedido (id_pedido),
  KEY idx_dpedido_producto (id_producto),
  CONSTRAINT fk_dpedido_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedido (id_pedido),
  CONSTRAINT fk_dpedido_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
  CONSTRAINT ck_dpedido_cantidad CHECK (cantidad > 0 AND precio_unitario >= 0)
) ENGINE=InnoDB;

CREATE TABLE pago (
  id_pago      INT            NOT NULL AUTO_INCREMENT,
  id_pedido    INT            NOT NULL,
  monto        DECIMAL(12,2)  NOT NULL,
  medio        ENUM('SIMULADO','POS','EFECTIVO','YAPE','PLIN') NOT NULL,
  -- PENDIENTE: pago Yape/Plin esperando que el vendedor verifique que el dinero llegó
  estado       ENUM('PENDIENTE','APROBADO','RECHAZADO') NOT NULL,
  referencia   VARCHAR(60)    NULL COMMENT 'N° de operación del voucher del POS o código simulado',
  fecha_pago   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comprobante       LONGBLOB     NULL COMMENT 'Captura del pago Yape/Plin que sube el cliente',
  comprobante_tipo  VARCHAR(30)  NULL COMMENT 'image/jpeg, image/png o image/webp',
  PRIMARY KEY (id_pago),
  KEY idx_pago_pedido (id_pedido),
  CONSTRAINT fk_pago_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido),
  CONSTRAINT ck_pago_monto CHECK (monto > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  VENTAS (salidas de inventario)
-- ---------------------------------------------------------------------
CREATE TABLE venta (
  id_venta          INT            NOT NULL AUTO_INCREMENT,
  fecha             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_cliente        INT            NULL COMMENT 'NULL = cliente general sin registrar',
  id_usuario        INT            NOT NULL COMMENT 'Vendedor o administrador que registra la venta',
  id_pedido         INT            NULL COMMENT 'Pedido en línea del que proviene la venta, si aplica',
  total             DECIMAL(12,2)  NOT NULL DEFAULT 0,
  tipo_comprobante  ENUM('BOLETA','FACTURA') NOT NULL DEFAULT 'BOLETA',
  PRIMARY KEY (id_venta),
  UNIQUE KEY uk_venta_pedido (id_pedido),
  KEY idx_venta_fecha (fecha),
  KEY idx_venta_cliente (id_cliente),
  KEY idx_venta_usuario (id_usuario),
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
  CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT fk_venta_pedido  FOREIGN KEY (id_pedido)  REFERENCES pedido (id_pedido)
) ENGINE=InnoDB;

CREATE TABLE detalle_venta (
  id_detalle_venta  INT            NOT NULL AUTO_INCREMENT,
  id_venta          INT            NOT NULL,
  id_producto       INT            NOT NULL,
  cantidad          INT            NOT NULL,
  precio_unitario   DECIMAL(10,2)  NOT NULL,
  subtotal          DECIMAL(12,2)  NOT NULL,
  PRIMARY KEY (id_detalle_venta),
  KEY idx_dventa_venta (id_venta),
  KEY idx_dventa_producto (id_producto),
  CONSTRAINT fk_dventa_venta    FOREIGN KEY (id_venta)    REFERENCES venta (id_venta),
  CONSTRAINT fk_dventa_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
  CONSTRAINT ck_dventa_cantidad CHECK (cantidad > 0 AND precio_unitario >= 0 AND subtotal >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  VISTA: stock disponible real (lo que muestra el catálogo público)
-- ---------------------------------------------------------------------
CREATE VIEW v_producto_disponible AS
SELECT p.id_producto, p.codigo_sku, p.nombre, p.descripcion, p.precio_venta,
       p.stock_actual, p.stock_reservado,
       (p.stock_actual - p.stock_reservado) AS stock_disponible,
       p.stock_minimo, p.id_categoria, c.nombre AS categoria,
       p.id_proveedor, p.estado
FROM producto p
JOIN categoria c ON c.id_categoria = p.id_categoria;

-- =====================================================================
--  DATOS INICIALES
-- =====================================================================
INSERT INTO rol (nombre_rol) VALUES ('ADMINISTRADOR'), ('VENDEDOR'), ('CLIENTE');

INSERT INTO categoria (nombre, descripcion) VALUES
  ('Filtros',      'Filtros de aceite, aire y combustible'),
  ('Frenos',       'Pastillas, discos y líquido de frenos'),
  ('Baterías',     'Baterías para autos y motos'),
  ('Lubricantes',  'Aceites y grasas'),
  ('Motor',        'Correas, bujías y piezas de motor'),
  ('Eléctrico',    'Sistema eléctrico y accesorios');

INSERT INTO proveedor (ruc, razon_social, nombre_comercial, direccion, persona_contacto, telefono, correo) VALUES
  ('20234356734', 'Solver S.A.C.',            'Solver',          'Av. Industrial 123, Lima', 'Adriana Espinoza', '987654321', 'ventas@solver.example'),
  ('20601122334', 'Repuestos del Norte S.A.', 'Repuestos Norte', 'Av. Túpac Amaru 456, Comas', 'Carlos Ramírez', '912345678', 'contacto@norte.example'),
  ('20456789012', 'Importadora Andina S.A.C.','Andina',          'Jr. Comercio 789, Lima',   'Lucía Torres',     '956781234', 'info@andina.example');

INSERT INTO producto (codigo_sku, nombre, descripcion, precio_venta, stock_actual, stock_reservado, stock_minimo, id_categoria, id_proveedor) VALUES
  ('F-1023', 'Filtro de aceite Toyota',   'Compatible con modelos Toyota',   28.00,   5, 0,  8, 1, 1),
  ('B-0456', 'Batería 12V 45Ah',          'Batería libre de mantenimiento', 260.00,  2, 0,  4, 3, 2),
  ('P-0789', 'Pastillas de freno delanteras', 'Juego de 4 pastillas',        65.00, 12, 0,  6, 2, 1),
  ('A-0022', 'Aceite sintético 4L',       'Aceite 5W-30 sintético',          95.00,  0, 0, 10, 4, 3),
  ('C-0311', 'Correa de distribución',    'Correa con tensor',               80.00,  3, 0,  5, 5, 2),
  ('E-0100', 'Bujía de iridio',           'Bujía de larga duración',         32.00, 20, 0, 10, 5, 3);

INSERT INTO usuario (nombres, correo, contrasena, id_rol, estado)
SELECT 'Administrador General', 'admin@electrospeed.com',
       'pbkdf2$120000$eQyUpiMbLOQNy0ZfJRulqw==$swFd8Wa07iVlXaSMSgBMKIIDExBgis1VdvpiVaDGaQk=',
       id_rol, 1 FROM rol WHERE nombre_rol = 'ADMINISTRADOR'
  AND NOT EXISTS (SELECT 1 FROM usuario WHERE correo = 'admin@electrospeed.com');

INSERT INTO usuario (nombres, correo, contrasena, id_rol, estado)
SELECT 'Vendedor de Prueba', 'vendedor@electrospeed.com',
       'pbkdf2$120000$wqrass/jvCMHVBRt5Gh+0Q==$SDnAioGUXOH6/dWD0Uf1IU7fWnAAm3bH9Kl7vjGSgK0=',
       id_rol, 1 FROM rol WHERE nombre_rol = 'VENDEDOR'
  AND NOT EXISTS (SELECT 1 FROM usuario WHERE correo = 'vendedor@electrospeed.com');

-- Verificación (opcional)
SELECT * FROM v_producto_disponible;
SELECT u.id_usuario, u.nombres, u.correo, r.nombre_rol, u.estado
FROM usuario u JOIN rol r ON r.id_rol = u.id_rol;
