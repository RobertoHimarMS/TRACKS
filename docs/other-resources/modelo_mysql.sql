-- ============================================================
-- TrackYours Sports - Script de creación de Base de Datos MySQL
-- ============================================================
-- Autor: Generado desde análisis de documentación del proyecto
-- Fecha: 2026-03-05
-- Descripción: Script DDL para crear el esquema de base de datos
--              del sistema de gestión de clubes deportivos
-- ============================================================

-- Crear base de datos (descomentar si es necesario)
-- CREATE DATABASE IF NOT EXISTS trackyours_db
--   CHARACTER SET utf8mb4
--   COLLATE utf8mb4_unicode_ci;
-- USE trackyours_db;

-- ============================================================
-- TABLA: persona
-- Descripción: Almacena los datos personales de usuarios y gestores
-- ============================================================
CREATE TABLE persona (
    dni VARCHAR(9) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    cp VARCHAR(10) NOT NULL,
    telefono VARCHAR(20),
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_persona PRIMARY KEY (dni)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: usuario
-- Descripción: Credenciales y estado de usuarios del sistema
-- ============================================================
CREATE TABLE usuario (
    email VARCHAR(254) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    estado ENUM('activo', 'inactivo', 'pendiente') NOT NULL DEFAULT 'pendiente',
    dni VARCHAR(9) NOT NULL,
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_usuario PRIMARY KEY (email),
    CONSTRAINT uq_usuario_dni UNIQUE (dni),
    CONSTRAINT fk_usuario_dni FOREIGN KEY (dni) REFERENCES persona(dni)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: club
-- Descripción: Clubes deportivos registrados en el sistema
-- ============================================================
CREATE TABLE club (
    id_club INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    cif VARCHAR(15),
    direccion VARCHAR(255) NOT NULL,
    cp VARCHAR(10) NOT NULL,
    deporte VARCHAR(100) NOT NULL,
    descripcion TEXT,
    estado ENUM('activo', 'inactivo', 'pendiente') NOT NULL DEFAULT 'pendiente',
    imagen VARCHAR(4000),
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_club PRIMARY KEY (id_club)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: peticion
-- Descripción: Solicitudes de alta de clubes o socios
-- ============================================================
CREATE TABLE peticion (
    id_peticion INT NOT NULL AUTO_INCREMENT,
    tipo ENUM('alta_club', 'alta_socio') NOT NULL,
    estado ENUM('pendiente', 'aprobada', 'rechazada') NOT NULL DEFAULT 'pendiente',
    dni_peticionario VARCHAR(9) NOT NULL,
    id_club_destino INT,
    email_validador VARCHAR(254),
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME,
    observaciones TEXT,
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_peticion PRIMARY KEY (id_peticion),
    CONSTRAINT fk_peticion_dni_peticionario FOREIGN KEY (dni_peticionario) REFERENCES persona(dni)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_peticion_id_club_destino FOREIGN KEY (id_club_destino) REFERENCES club(id_club)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_peticion_email_validador FOREIGN KEY (email_validador) REFERENCES usuario(email)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: usuario_club
-- Descripción: Relación entre usuarios y clubes (socios y gestores)
-- ============================================================
CREATE TABLE usuario_club (
    email VARCHAR(254) NOT NULL,
    id_club INT NOT NULL,
    rol ENUM('socio', 'gestor', 'admin') NOT NULL DEFAULT 'socio',
    cuota_pagada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_alta DATE NOT NULL DEFAULT (CURRENT_DATE),
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_usuario_club PRIMARY KEY (email, id_club),
    CONSTRAINT fk_usuario_club_email FOREIGN KEY (email) REFERENCES usuario(email)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_club_id_club FOREIGN KEY (id_club) REFERENCES club(id_club)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: actividad
-- Descripción: Actividades organizadas por los clubes
-- ============================================================
CREATE TABLE actividad (
    id_actividad INT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    modalidad ENUM('running', 'natacion', 'bicicleta', 'sendero', 'otra'),
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    lugar VARCHAR(255),
    distancia_km DECIMAL(10,2),
    id_club INT NOT NULL,
    imagen VARCHAR(4000),
    estado ENUM('programada', 'en_curso', 'finalizada', 'cancelada') NOT NULL DEFAULT 'programada',
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_actividad PRIMARY KEY (id_actividad),
    CONSTRAINT fk_actividad_id_club FOREIGN KEY (id_club) REFERENCES club(id_club)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: inscripcion
-- Descripción: Inscripciones de usuarios a actividades
-- ============================================================
CREATE TABLE inscripcion (
    email VARCHAR(254) NOT NULL,
    id_actividad INT NOT NULL,
    fecha_inscripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('inscrito', 'confirmado', 'cancelado') NOT NULL DEFAULT 'inscrito',
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_inscripcion PRIMARY KEY (email, id_actividad),
    CONSTRAINT fk_inscripcion_email FOREIGN KEY (email) REFERENCES usuario(email)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_inscripcion_id_actividad FOREIGN KEY (id_actividad) REFERENCES actividad(id_actividad)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: publicacion
-- Descripción: Publicaciones y noticias de los clubes
-- ============================================================
CREATE TABLE publicacion (
    id_publicacion INT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(100) NOT NULL,
    texto TEXT NOT NULL,
    imagen VARCHAR(4000),
    id_club INT NOT NULL,
    fecha_publicacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    autor VARCHAR(100),
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_publicacion PRIMARY KEY (id_publicacion),
    CONSTRAINT fk_publicacion_id_club FOREIGN KEY (id_club) REFERENCES club(id_club)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: producto
-- Descripción: Productos de la tienda del club
-- ============================================================
CREATE TABLE producto (
    id_producto INT NOT NULL AUTO_INCREMENT,
    tangible BOOLEAN NOT NULL DEFAULT TRUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    id_club INT NOT NULL,
    imagen VARCHAR(4000),
    estado ENUM('disponible', 'agotado', 'retirado') NOT NULL DEFAULT 'disponible',
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_producto PRIMARY KEY (id_producto),
    CONSTRAINT fk_producto_id_club FOREIGN KEY (id_club) REFERENCES club(id_club)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: compra
-- Descripción: Compras realizadas por usuarios
-- ============================================================
CREATE TABLE compra (
    id_compra INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    estado ENUM('pendiente', 'confirmada', 'entregada', 'cancelada') NOT NULL DEFAULT 'pendiente',
    fecha_compra DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_compra PRIMARY KEY (id_compra),
    CONSTRAINT fk_compra_email FOREIGN KEY (email) REFERENCES usuario(email)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_compra_id_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_compra_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_compra_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: ticket
-- Descripción: Tickets de soporte del sistema
-- ============================================================
CREATE TABLE ticket (
    id_ticket INT NOT NULL AUTO_INCREMENT,
    asunto VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    prioridad ENUM('baja', 'media', 'alta', 'urgente') DEFAULT 'media',
    estado ENUM('abierto', 'en_proceso', 'resuelto', 'cerrado') NOT NULL DEFAULT 'abierto',
    dni_creador VARCHAR(9) NOT NULL,
    email_atendedor VARCHAR(254),
    fecha_creacion_ticket DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME,
    -- Campos de auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor_modificacion VARCHAR(100),
    CONSTRAINT pk_ticket PRIMARY KEY (id_ticket),
    CONSTRAINT fk_ticket_dni_creador FOREIGN KEY (dni_creador) REFERENCES persona(dni)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_email_atendedor FOREIGN KEY (email_atendedor) REFERENCES usuario(email)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- ============================================================

-- Índice para búsquedas por nombre de club
CREATE INDEX idx_club_nombre ON club(nombre);

-- Índice para búsquedas por estado de club
CREATE INDEX idx_club_estado ON club(estado);

-- Índice para búsquedas por deporte
CREATE INDEX idx_club_deporte ON club(deporte);

-- Índice para búsquedas por código postal
CREATE INDEX idx_persona_cp ON persona(cp);

-- Índice para búsquedas por DNI en persona
CREATE INDEX idx_persona_dni ON persona(dni);

-- Índice para estado de usuario
CREATE INDEX idx_usuario_estado ON usuario(estado);

-- Índice para estado de peticiones
CREATE INDEX idx_peticion_estado ON peticion(estado);

-- Índice para tipo de peticiones
CREATE INDEX idx_peticion_tipo ON peticion(tipo);

-- Índice para búsquedas de actividades por fecha
CREATE INDEX idx_actividad_fecha ON actividad(fecha);

-- Índice para búsquedas de actividades por club
CREATE INDEX idx_actividad_club ON actividad(id_club);

-- Índice para estado de tickets
CREATE INDEX idx_ticket_estado ON ticket(estado);

-- Índice para prioridad de tickets
CREATE INDEX idx_ticket_prioridad ON ticket(prioridad);

-- Índice para estado de productos
CREATE INDEX idx_producto_estado ON producto(estado);

-- Índice para estado de compras
CREATE INDEX idx_compra_estado ON compra(estado);

-- ============================================================
-- VISTAS ÚTILES
-- ============================================================

-- Vista de socios con información completa
CREATE OR REPLACE VIEW v_socios_completos AS
SELECT
    p.dni,
    p.nombre,
    p.apellidos,
    p.direccion,
    p.cp,
    p.telefono,
    u.email,
    u.estado AS estado_usuario,
    uc.id_club,
    c.nombre AS nombre_club,
    uc.rol,
    uc.cuota_pagada,
    uc.fecha_alta
FROM persona p
JOIN usuario u ON p.dni = u.dni
JOIN usuario_club uc ON u.email = uc.email
JOIN club c ON uc.id_club = c.id_club;

-- Vista de actividades con información del club
CREATE OR REPLACE VIEW v_actividades_club AS
SELECT
    a.id_actividad,
    a.titulo,
    a.descripcion,
    a.modalidad,
    a.fecha,
    a.hora_inicio,
    a.lugar,
    a.distancia_km,
    a.estado,
    c.id_club,
    c.nombre AS nombre_club,
    c.deporte,
    (SELECT COUNT(*) FROM inscripcion i WHERE i.id_actividad = a.id_actividad) AS num_inscritos
FROM actividad a
JOIN club c ON a.id_club = c.id_club;

-- Vista de peticiones pendientes
CREATE OR REPLACE VIEW v_peticiones_pendientes AS
SELECT
    p.id_peticion,
    p.tipo,
    p.fecha_solicitud,
    per.nombre AS nombre_peticionario,
    per.apellidos AS apellidos_peticionario,
    per.dni AS dni_peticionario,
    c.nombre AS nombre_club,
    p.estado
FROM peticion p
JOIN persona per ON p.dni_peticionario = per.dni
LEFT JOIN club c ON p.id_club_destino = c.id_club
WHERE p.estado = 'pendiente'
ORDER BY p.fecha_solicitud ASC;

-- Vista de tickets abiertos
CREATE OR REPLACE VIEW v_tickets_abiertos AS
SELECT
    t.id_ticket,
    t.asunto,
    t.prioridad,
    t.fecha_creacion_ticket,
    per.nombre AS nombre_creador,
    per.apellidos AS apellidos_creador,
    t.estado,
    u.email AS email_atendedor
FROM ticket t
JOIN persona per ON t.dni_creador = per.dni
LEFT JOIN usuario u ON t.email_atendedor = u.email
WHERE t.estado IN ('abierto', 'en_proceso')
ORDER BY
    CASE t.prioridad
        WHEN 'urgente' THEN 1
        WHEN 'alta' THEN 2
        WHEN 'media' THEN 3
        WHEN 'baja' THEN 4
    END,
    t.fecha_creacion_ticket ASC;

-- ============================================================
-- DATOS DE PRUEBA (OPCIONAL - Descomentar si se necesitan)
-- ============================================================

-- INSERT INTO persona (dni, nombre, apellidos, direccion, cp, telefono) VALUES
-- ('12345678A', 'Admin', 'Sistema', 'Calle Principal 1', '28001', '600123456');

-- INSERT INTO usuario (email, contrasena, estado, dni) VALUES
-- ('admin@trackyours.com', '$2a$10$...', 'activo', '12345678A');

-- INSERT INTO club (nombre, cif, direccion, cp, deporte, descripcion, estado) VALUES
-- ('Club Running Madrid', 'B12345678', 'Calle Deporte 10', '28001', 'running', 'Club de running en Madrid', 'activo');

-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================