-- ----------------------------------------------------------------------
-- ## SCRIPT DE INICIALIZACIÓN/CREACIÓN DE LA BASE DE DATOS            --
-- ## APppWeb: TrackYours  (esquema: db_tracks)                        --
-- ## Para MySQL 8.0.34                                                --
-- ----------------------------------------------------------------------


-- -----------------------------------------------------------
-- 1. Formateo previo y Creación del Esquema
-- -----------------------------------------------------------
DROP DATABASE IF EXISTS db_tracks;
CREATE DATABASE db_tracks CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_tracks;

-- Desactivamos chequeos de FK para crear tablas con dependencias circulares
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 2. Definición de Tablas
-- -----------------------------------------------------------

-- Tabla Ticket
-- Incluye email directamente (sin FK a Visitor) y campos auditoría
CREATE TABLE Ticket (
    idticket INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(40) NOT NULL,
    description VARCHAR(240) NOT NULL,
	email VARCHAR(50) NOT NULL,
    handled BOOLEAN NOT NULL DEFAULT FALSE,
    aud_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aud_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,										
    aud_updated_by INT UNSIGNED NULL DEFAULT NULL													-- actualizar desde trigger
) ENGINE=InnoDB;

-- Tabla Request (Solicitud)
-- Incluye email directo (sin FK a Visitor) y campos de auditoría
CREATE TABLE Request (
    idrequest INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('club', 'partner') NOT NULL,
    estado ENUM('pending', 'accepted', 'rejected') NOT NULL,
    clb_target VARCHAR(50) NOT NULL,
    clb_description VARCHAR(240) NOT NULL,
    clb_sport VARCHAR(20) NOT NULL,
	clb_email VARCHAR(50) NOT NULL,
    clb_cp VARCHAR(8),
    clb_city VARCHAR(40),
    clb_photo VARCHAR(240),
    usr_dni VARCHAR(20) NOT NULL,
    usr_name VARCHAR(40) NOT NULL,
    usr_surname VARCHAR(40) NOT NULL,
	usr_email VARCHAR(50)  NOT NULL,
    usr_passwd VARCHAR(255) NOT NULL,                      											-- ampliado para hashes
    usr_cp VARCHAR(8),
    usr_city VARCHAR(40),
    usr_borned DATE,
    usr_phone VARCHAR(20),
    usr_photo VARCHAR(240),
    aud_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aud_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,										
    aud_updated_by INT UNSIGNED NULL DEFAULT NULL													-- actualizar desde trigger
) ENGINE=InnoDB;

-- Tabla Users (Usuario)
-- Se crean desde un primer Request (ya sea como Manager o Partner). Usa el email como userid en la App (único). Dni y email deben ser únicos
CREATE TABLE Users (
    iduser INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL,
    name VARCHAR(40) NOT NULL,
    surname VARCHAR(40) NOT NULL,
    email VARCHAR(50) NOT NULL,
    passwd VARCHAR(255) NOT NULL,                            										-- ampliado para hashes
    cp VARCHAR(8),
    city VARCHAR(40),
    borned DATE,
    phone VARCHAR(20),
    photo VARCHAR(240),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    aud_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aud_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,										
    aud_updated_by INT UNSIGNED NULL DEFAULT NULL,													-- actualizar desde trigger															
    Request_idrequest INT UNSIGNED NULL,
	UNIQUE KEY uniq_users_dni (dni),
    UNIQUE KEY uniq_users_email (email)
) ENGINE=InnoDB;

-- Tabla Club
-- Incluye aud_modified_by y UNIQUE en nombre y email del club
CREATE TABLE Club (
    idclub INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(240) NOT NULL,
    sport VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    cp VARCHAR(8),
    city VARCHAR(40),
    photo VARCHAR(240),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    aud_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aud_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,										
    aud_updated_by INT UNSIGNED NULL DEFAULT NULL,													-- actualizar desde trigger
    Request_idrequest INT UNSIGNED NULL,
	UNIQUE KEY uniq_club_name (name),
	UNIQUE KEY uniq_club_email (email)   
) ENGINE=InnoDB;

-- Tabla Actividad (sin auditoria. El club puede borrar actividades y sus respectivas inscripciones sin afección legal)
CREATE TABLE Actividad (
    idactividad INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(40) NOT NULL,
    description VARCHAR(240) NOT NULL,
    sport VARCHAR(20) NOT NULL,
    fecha DATETIME NOT NULL,
    place VARCHAR(40) NOT NULL,
    distancia INT,
    photo VARCHAR(240),
    Club_idclub INT UNSIGNED NOT NULL
) ENGINE=InnoDB;

-- Tabla Producto (sin auditoría. App nunca permite borrar producto por lógica de negocio, sólo poner stock a 0) 
CREATE TABLE Producto (
    idproducto INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    description VARCHAR(240) NOT NULL,
    photo VARCHAR(240),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 1,
    Club_idclub INT UNSIGNED NOT NULL
) ENGINE=InnoDB;

-- Tabla Publicacion (sin auditoria. El club puede borrar una publicación sin afección legal alguna, son suyas)
CREATE TABLE Publicacion (
    idpublicacion INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(40) NOT NULL,
    text VARCHAR(240) NOT NULL,
    photo VARCHAR(240),
    Club_idclub INT UNSIGNED NOT NULL
) ENGINE=InnoDB;

-- Tabla pertenece_a (Relación Users-Club). Única modificaciones posibles: alta o baja
CREATE TABLE pertenece_a (
    Users_iduser INT UNSIGNED NOT NULL,
    Club_idclub INT UNSIGNED NOT NULL,
    rol ENUM('admin', 'manager', 'partner') NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unsuscribed_at TIMESTAMP NULL DEFAULT NULL,								-- actualizar desde lógica de la AppWeb										
    PRIMARY KEY (Users_iduser, Club_idclub)
) ENGINE=InnoDB;

-- Tabla se_inscribe (Relación Users-Actividad). Sin auditoría, inscribirse crea registro, desinscribirse borra registro
CREATE TABLE se_inscribe (
    Users_iduser INT UNSIGNED NOT NULL,
    Actividad_idactividad INT UNSIGNED NOT NULL,
    PRIMARY KEY (Users_iduser, Actividad_idactividad)											-- PK combinada (nunca se repetirá posque las inscripciones se borran literalmente
) ENGINE=InnoDB;

-- Tabla compra (Relación Users-Producto). Las compras si requieren auditoría (hay dinero de por medio)
CREATE TABLE compra (
    idcompra INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,									-- PK (debe ser así para que un User pueda compar el mismo producto varias veces)
	Users_iduser INT UNSIGNED NOT NULL,															-- una FK 
    Producto_idproducto INT UNSIGNED NOT NULL,													-- otra FK
    cantidad INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado ENUM('reserved', 'paid', 'collected', 'cancelled') NOT NULL DEFAULT 'reserved',  -- estados: pendiente, pagado, entregado, cancelado
    aud_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aud_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,										
    aud_updated_by INT UNSIGNED NULL DEFAULT NULL												-- actualizar desde trigger
) ENGINE=InnoDB;


-- -----------------------------------------------------------
-- 3. Reactivación de Chequeos de FK
-- -----------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------
-- 4. Creación de Claves Foráneas (Constraints)
-- -----------------------------------------------------------

-- FKs para Ticket
ALTER TABLE Ticket ADD CONSTRAINT Ticket_Users_FK FOREIGN KEY (aud_updated_by) REFERENCES Users (iduser);

-- FKs para Request
ALTER TABLE Request ADD CONSTRAINT Request_Users_FK FOREIGN KEY (aud_updated_by) REFERENCES Users (iduser);

-- FKs para Users
ALTER TABLE Users ADD CONSTRAINT Users_Request_FK FOREIGN KEY (Request_idrequest) REFERENCES Request (idrequest);
ALTER TABLE Users ADD CONSTRAINT Users_Users_FK FOREIGN KEY (aud_updated_by) REFERENCES Users (iduser);						-- autoreferencia

-- FKs para Club 
ALTER TABLE Club ADD CONSTRAINT Club_Request_FK FOREIGN KEY (Request_idrequest) REFERENCES Request (idrequest);
ALTER TABLE Club ADD CONSTRAINT Club_Users_FK FOREIGN KEY (aud_updated_by) REFERENCES Users (iduser);

-- FKs para Actividad
ALTER TABLE Actividad ADD CONSTRAINT Actividad_Club_FK FOREIGN KEY (Club_idclub) REFERENCES Club (idclub);

-- FKs para Publicacion
ALTER TABLE Publicacion ADD CONSTRAINT Publicacion_Club_FK FOREIGN KEY (Club_idclub) REFERENCES Club (idclub);

-- FKs para Producto
ALTER TABLE Producto ADD CONSTRAINT Producto_Club_FK FOREIGN KEY (Club_idclub) REFERENCES Club (idclub);

-- FKs para tablas de relación M:N
ALTER TABLE pertenece_a ADD CONSTRAINT pertenece_a_Club_FK FOREIGN KEY (Club_idclub) REFERENCES Club (idclub);
ALTER TABLE pertenece_a ADD CONSTRAINT pertenece_a_Users_FK FOREIGN KEY (Users_iduser) REFERENCES Users (iduser);

ALTER TABLE se_inscribe ADD CONSTRAINT se_inscribe_Actividad_FK FOREIGN KEY (Actividad_idactividad) REFERENCES Actividad (idactividad);
ALTER TABLE se_inscribe ADD CONSTRAINT se_inscribe_Users_FK FOREIGN KEY (Users_iduser) REFERENCES Users (iduser);

ALTER TABLE compra ADD CONSTRAINT compra_Users_FK_1 FOREIGN KEY (Users_iduser) REFERENCES Users (iduser);
ALTER TABLE compra ADD CONSTRAINT compra_Producto_FK FOREIGN KEY (Producto_idproducto) REFERENCES Producto (idproducto);
ALTER TABLE compra ADD CONSTRAINT compra_Users_FK_2 FOREIGN KEY (aud_updated_by) REFERENCES Users (iduser);



-- -----------------------------------------------------------
-- 5. Los Triggers de la Auditorías implementadas
-- -----------------------------------------------------------

DELIMITER $$

-- ==========================================
-- TRIGGERS PARA LA TABLA: Ticket
-- Campos Auditoría: aud_created_at (auto), aud_updated_at (auto), aud_updated_by (trigger)
-- Nota: El campo de estado es 'handled' (booleano), y se maneja desde lógica de la App
-- ==========================================
CREATE TRIGGER trg_ticket_before_insert
BEFORE INSERT ON Ticket FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito para aud_updated_by
END$$													-- el trigger no hace nada y se inserta el explicito, lo respetamos (migración/seeds)

CREATE TRIGGER trg_ticket_before_update
BEFORE UPDATE ON Ticket FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el UPDATE no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el UPDATE trae valor explícito para aud_updated_by,
END$$													-- el trigger no hace nada y se actualiza con el explícito, lo respetamos (AppWeb)

-- ==========================================
-- TRIGGERS PARA LA TABLA: Request
-- Campos Auditoría: aud_created_at (auto), aud_updated_at (auto), aud_updated_by
-- Nota: Solo tenemos auditoría genérica. El campo de estado es 'estado'.
-- ==========================================
CREATE TRIGGER trg_request_before_insert
BEFORE INSERT ON Request FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito para aud_updated_by
END$$													-- el trigger no hace nada y se inserta el explicito, lo respetamos (migración/seeds)

CREATE TRIGGER trg_request_before_update
BEFORE UPDATE ON Request FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el UPDATE no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el UPDATE trae valor explícito para aud_updated_by,
END$$													-- el trigger no hace nada y se actualiza con el explícito, lo respetamos (AppWeb)

-- ========================================
-- TRIGGERS PARA LA TABLA: Users
-- Campos Auditoría: aud_created_at (auto), aud_updated_at (auto), aud_updated_by
-- Nota: 
-- ===============================================================================================================
CREATE TRIGGER trg_users_before_insert
BEFORE INSERT ON Users FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito para aud_updated_by
END$$													-- el trigger no hace nada y se inserta el explicito, lo respetamos (migración/seeds)BEGIN

CREATE TRIGGER trg_users_before_update
BEFORE UPDATE ON Users FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el UPDATE no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el UPDATE trae valor explícito para aud_updated_by,
END$$													-- el trigger no hace nada y se actualiza con el explícito, lo respetamos (AppWeb)

-- ==========================================
-- TRIGGERS PARA LA TABLA: Club
-- Campos Auditoría: aud_created_at (auto), aud_updated_at (auto), aud_updated_by
-- Nota: 
-- ==========================================
CREATE TRIGGER trg_club_before_insert
BEFORE INSERT ON Club FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito para aud_updated_by
END$$													-- el trigger no hace nada y se inserta el explicito, lo respetamos (migración/seeds)

CREATE TRIGGER trg_club_before_update
BEFORE UPDATE ON Club FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el UPDATE no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el UPDATE trae valor explícito para aud_updated_by,
END$$													-- el trigger no hace nada y se actualiza con el explícito, lo respetamos (AppWeb)

-- ==========================================
-- TRIGGERS PARA LA TABLA: compra
-- Campos Auditoría: aud_created_at (auto), aud_updated_at (auto), aud_updated_by
-- Nota: Importante la forma de transmitir a el motor de BD el usuario logueado (ver comentario más abajo)
-- ==========================================
CREATE TRIGGER trg_compra_before_insert
BEFORE INSERT ON compra FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el INSERT no trae valor explícito para aud_updated_by
END$$													-- el trigger no hace nada y se inserta el explicito, lo respetamos (migración/seeds)

CREATE TRIGGER trg_compra_before_update
BEFORE UPDATE ON compra FOR EACH ROW
BEGIN
    IF @app_user_id IS NOT NULL THEN        		-- Caso 1: Hay usuario en sesión. Lo usamos.
        SET NEW.aud_updated_by = @app_user_id;
    ELSEIF NEW.aud_updated_by IS NULL THEN        	-- Caso 2: No hay sesión (@app_user_id es NULL) Y el UPDATE no trae valor explícito (es NULL),
		SET NEW.aud_updated_by = 1;						-- entonces ponemos el valor por defecto (System).
	END IF;											-- Caso 3: No hay sesión (@app_user_id es NULL) Y el UPDATE trae valor explícito para aud_updated_by,
END$$													-- el trigger no hace nada y se actualiza con el explícito, lo respetamos (AppWeb)

DELIMITER ;


-- ---------------------------------------------------------------------------
-- IMPORTANTE para triggers >>> Instrucciones para la Aplicación Web (Backend)
-- ----------------------------------------------------------------------------
/*
   IMPORTANTE: USO DE LA VARIABLE DE AUDITORÍA (@app_user_id)

   Los triggers definidos arriba utilizan la variable de sesión @app_user_id 
   para registrar automáticamente quién realiza las operaciones de modificación 
   (INSERT o UPDATE) en las tablas Users, Club, Request y Ticket.

   Para que esto funcione correctamente, la aplicación web (Backend) DEBE 
   ejecutar la siguiente sentencia SQL inmediatamente después de abrir 
   la conexión a la base de datos para cada usuario logueado:

   SET @app_user_id = <ID_DEL_USUARIO>;

   Ejemplo práctico:
   Si el usuario 'admin' tiene el ID 36 en la tabla Users, al iniciar su sesión 
   el script de conexión debería ejecutar:
   
   SET @app_user_id = 36;

   Si el usuario no está logueado (p.ej. un visitante creando un ticket), 
   puedes setearlo a NULL o a un ID genérico de sistema:
   
   SET @app_user_id = NULL;

   Sin este paso, los campos aud_updated_by quedarían con valor NULL.
   
   De hecho nosotros crearemos dos usuarios "dummy" de sistema:
   
	==>>  iduser=1. Usuario "system" para acciones de BD tipo Batch 				(pertenecerá al club "System" "dummy" tbm)
	==>>  iduser=2. Usuario "admin" para gestión de la AppWeb, con rol admin        (pertenecerá al club "System" "dummy" tbm)
	==>>  idclub=1. Club "System" para usuarios de sistema 							
*/


-- -----------------------------------------------------------
-- 6. Inserción de Datos Semilla (Usuarios del Sistema)
-- -----------------------------------------------------------

-- Insertar Usuarios Dummy: ID=1(system) e ID=2(admin)
-- Request_idrequest es NULL porque son usuarios internos, no registros web públicos.
-- aud_updated_by será 1 (el admin) o 0 (el propio sistema)
-- por ahora contraseñas de entorno pre-explotación y didáctico
INSERT INTO Users (iduser, dni, name, surname, email, passwd, active, Request_idrequest) 
VALUES 
	(1, '00000000A', 'System', 'system', 'system@trackyours.com', '{noop}1234', TRUE, NULL), 	-- Usuario "System" para operaciones sin loguear
	(2, '99999999B', 'Admin', 'system', 'admin@trackyours.com', '{noop}1234', TRUE, NULL);   	-- Usuario Administrador del sistema

-- Insertar Club ficticio "System" (ID 1) para dar cobijo a los usuarios del sistema
-- Request_idrequest es NULL porque no proviene de una solicitud pública.
-- aud_updated_by quedará a 1 por actualizado desde trigger sin indicar @app_user_id
INSERT INTO Club (idclub, name, description, sport, email, active, Request_idrequest) 
VALUES 
	(1, 'System', 'Club ficticio de sistema', 'system', 'system@trackyours.com', TRUE, NULL);

-- Asignar roles en el club ficticio
INSERT INTO pertenece_a (Users_iduser, Club_idclub, rol) 
VALUES 
	(1, 1, 'partner'),  -- El usuario System es partner del club sistema (sólo destinado a auditoría de acciones en BD sim loguear)
	(2, 1, 'admin');    -- El usuario Admin es admin del club sistema (este si trabaja en la lógica de la Appweb)

-- Ajustar los autoincrementos para que los próximos registros (los reales) empiecen en 10
ALTER TABLE Users AUTO_INCREMENT = 10;
ALTER TABLE Club AUTO_INCREMENT = 10;
-- Ajustar los autoincrementos para que los próximos registros (los reales) empiecen en 1000 para estas tablas
ALTER TABLE Actividad AUTO_INCREMENT = 1000;
ALTER TABLE Publicacion AUTO_INCREMENT = 1000;
ALTER TABLE Producto AUTO_INCREMENT = 1000;