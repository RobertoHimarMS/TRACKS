

-- -----------------------------------------------------------
-- 7. Inserción de Datos de Prueba (Registros)
-- -----------------------------------------------------------
USE db_tracks;

-- Establecemos variable de usuario para los triggers de auditoría (Simulamos que el admin crea los registros)
-- SET @app_user_id = 2;  (no procede, se hace a mano en carga inicial de datos base)

-- Desactivamos chequeos de FK temporalmente para insertar en tablas con dependencias circulares
SET FOREIGN_KEY_CHECKS = 0;

-- 7.1. Tickets
-- Requisito: "genera 4 registros, dos en estado pending y dos en estado handled"
-- Los handled=F son creados por usuarios no logueados (Sytem) y los handled=T han sido modificados por el Admin (único que puede)
-- Id creado por auto_incremento
INSERT INTO Ticket (subject, description, email, handled, aud_updated_by)
VALUES
	('Problema de acceso', 'No puedo iniciar sesion en la aplicacion movil', 'roberto@gmail.com', FALSE, 1),
	('Duda sobre cuota', '¿Cual es la fecha limite de pago del trimestre en el club AA?', 'alejandro@gmail.com', TRUE, 2),
	('Error en perfil', 'Mi foto de perfil no se carga correctamente', 'paco@gmail.com', FALSE, 1),
	('Sugerencia de horario', 'Seria bueno abrir antes los fines de semana', 'roberto@gmail.com', TRUE, 2);

-- 7.2. Requests
-- Las que han dado lugar a los Clubs y a sus gestores y socios. Todas aceptadas. 5 Clubes (con sus gestores) + 2 socios extra 
-- Dos peticione pendientes (una de club y otra de socio). Y una rechazada de club.
--
INSERT INTO Request (idrequest, tipo, estado, clb_target, clb_description, clb_sport, clb_email, clb_cp, clb_city, clb_photo,
				  usr_dni, usr_name, usr_surname, usr_email, usr_passwd, usr_cp, usr_city, usr_borned, usr_phone, usr_photo, aud_updated_by) 
VALUES 
	(1, 'club', 'accepted', 'Club AA', 'Club de running', 'run', 'info@clubaa.com', '35000', 'Las Palmas', 'run.jpg', '11111111A',
					'Gestor', 'AA', 'gestoraa@gmail.com', '1234', '35000', 'Las Palmas', '1978-04-04', '666-656565', 'gestoraa.jpg', 2),  	-- club aceptada
	(2, 'club', 'accepted', 'Club BB', 'Club de baloncesto', 'basket', 'info@clubbb.com', '35000', 'Las Palmas', 'basket.jpg', '22222222B',
					'Gestor', 'BB', 'gestorbb@gmail.com', '1234', '35000', 'Las Palmas', '1980-06-17', '689-353535', 'gestorbb.jpg', 2), 	 -- club aceptada
	(3, 'club', 'accepted', 'Club CC', 'Club de natación', 'swim', 'info@clubcc.com', '38000', 'Tenerife', 'swim.jpg', '33333333C',
					'Gestor', 'CC', 'gestorcc@gmail.com', '1234', '38000', 'Tenerife', '1985-03-01', '922-236598', 'gestrocc.jpg', 2),    	-- club aceptada
	(4, 'club', 'accepted', 'Club DD', 'Club de senderismo', 'hike', 'info@clubdd.com', '35000', 'Las Palmas', 'hike.jpg', '44444444D',
					'Gestor', 'DD', 'gestordd@gmail.com', '1234', '35000', 'Las Palmas', '1975-11-13', '928-656565', 'gestordd.jpg', 2),   	-- club aceptada
	(5, 'club', 'accepted', 'Club EE', 'Club de trail', 'trail', 'info@clubee.com', '38350', 'Tacoronte', 'trail.jpg', '55555555E',
					'Gestor', 'EE', 'gestoree@gmail.com', '1234', '38350', 'Tacoronte', '1990-03-07', '699-333222', 'gestoree.jpg', 2),   	-- club aceptada
	(6, 'club', 'accepted', 'Club FF', 'Club de balonmano', 'handball', 'info@clubff.com', '38003', 'Ifara', 'handball.jpg', '66666666F',
					'Gestor', 'FF', 'gestorff@gmail.com', '1234', '38003', 'Ifara', '1999-12-12', '677-377677', 'gestorff.jpg', 2),   		-- club aceptada (pero ya de baja)
	(7, 'club', 'pending', 'Club YY', 'Club de petanca', 'petanca', 'info@clubyy.com', '35400', 'Arucas', 'petanca.jpg', '88888888Y',
					'Gestor', 'YY', 'gestoryy@gmail.com', '{noop}1234', '35400', 'Arucas', '1995-10-10', '677-878787', 'gestoryy.jpg', 1),  		-- club pendiente
	(8, 'club', 'rejected', 'Club ZZ', 'Club de tennis', 'tennis', 'info@clubzz.com', '38000', 'Sta Cruz', 'tennis.jpg', '99999999Z',
					'Gestor', 'ZZ', 'gestorzz@gmail.com', '1234', '38000', 'Santa Cruz', '1987-07-23', '633-125998', 'gestorzz.jpg', 2),  	-- club rechazada
	(9, 'partner', 'accepted', 'Club AA', 'Club de running', 'run', 'info@clubaa.com', '35000', 'Las Palmas', '100-run.jpg', '44323322E',
					'Roberto', 'MS', 'robertoms@gmail.com', 'rhms', '35000', 'Las Palmas', '1978-04-21', '679-581971', 'roberto.jpg', 2), 	-- socio aceptada
	(10, 'partner', 'accepted', 'Club AA', 'Club de running', 'run', 'info@clubaa.com', '35000', 'Las Palmas', '100-run.jpg', '42722886H',
					'Donato', 'MR', 'donatomr@gmail.com', 'rdmr', '35414', 'Arucas', '1952-01-17', '688-566566', 'donato.jpg', 2),     	-- socio aceptada
	(11, 'partner', 'accepted', 'Club BB', 'Club de baloncesto', 'basket', 'info@clubbb.com', '35000', 'Las Palmas', '101-basket.jpg', '42722886H',
					'Donato', 'MR', 'donatomr@gmail.com', 'rdmr', '35414', 'Arucas', '1952-01-17', '688-566566', '17-donato.jpg', 2),     	-- socio aceptada (2nd club)
	(12, 'partner', 'accepted', 'Club FF', 'Club de balonmano', 'handball', 'info@clubff.com', '38003', 'Ifara', '105-handball.jpg', '42722886H',
					'Donato', 'MR', 'donatomr@gmail.com', 'rdmr', '35414', 'Arucas', '1952-01-17', '688-566566', '17-donato.jpg', 2),     	-- socio aceptada (3rd club, pero de baja))
	(13, 'partner', 'pending', 'Club AA', 'Club de running', 'run', 'info@clubaa.com', '35000', 'Las Palmas', '100-run.jpg', '42769320S',
					'Nieves', 'SM', 'nievessm@gmail.com', '{noop}mnsm', '35414', 'Arucas', '1954-08-31', '675-753753', 'nieves.jpg', 1);     	-- socio pendiente (sin foto en almacenamiento temporal /var/tracksyours/uploads)

-- 7.2. Usuarios
-- Derivados de las requests aceptadas anteriores (5 gestores + 2 socios del club AA)
-- Usuarios empiezan sus ids en 10 (id=1 y id=2 reservados para system y admin respectivamente)
-- Se deben asociar a las peticiones (requests) aceptadas que los generaron
INSERT INTO Users (iduser, dni, name, surname, email, passwd, cp, city, borned, phone, photo, active, aud_updated_by, Request_idrequest)
VALUES
	(10, '11111111A', 'Gestor', 'AA', 'gestoraa@gmail.com', '{noop}1234', '35000', 'Las Palmas', '1978-04-04', '666-656565', '10-gestoraa.jpg', TRUE, 2, 1),  	-- aceptado por admin en request 1 (gestor de AA)
	(11, '22222222B', 'Gestor', 'BB', 'gestorbb@gmail.com', '{noop}1234', '35000', 'Las Palmas', '1980-06-17', '689-353535', '11-gestorbb.jpg', TRUE, 2, 2),  	-- aceptado por admin en request 2 (gestor de BB)
	(12, '33333333C', 'Gestor', 'CC', 'gestorcc@gmail.com', '{noop}1234', '38000', 'Tenerife', '1985-03-01', '922-236598', '12-gestorcc.jpg', TRUE, 2, 3),  		-- aceptado por admin en request 3 (gestor de CC)
	(13, '44444444D', 'Gestor', 'DD', 'gestordd@gmail.com', '{noop}1234', '35000', 'Las Palmas', '1975-11-13', '928-656565', '13-gestordd.jpg', TRUE, 2, 4),  	-- aceptado por admin en request 4 (gestor de DD)
	(14, '55555555E', 'Gestor', 'EE', 'gestoree@gmail.com', '{noop}1234', '38350', 'Tacoronte', '1990-03-07', '699-333222', '14-gestoree.jpg', TRUE, 2, 5),  		-- aceptado por admin en request 5 (gestor de EE)
	(15, '66666666F', 'Gestor', 'FF', 'gestorff@gmail.com', '{noop}1234', '38003', 'Ifara', '1999-12-12', '677-377677', '15-gestorff.jpg', TRUE, 2, 6),  			-- aceptado por admin en request 5 (gestor de EE, de baja el club pero el mantiene perfil)
	(16, '44323322E', 'Roberto', 'MS', 'robertoms@gmail.com', '{noop}rhms', '35000', 'Las Palmas', '1978-04-21', '679-581971', '16-roberto.jpg', TRUE, 10, 9),  -- aceptado por admin en request 9 (socio de AA)
	(17, '42722886H', 'Donato', 'MR', 'donatomr@gmail.com', '{noop}rdmr', '35414', 'Arucas', '1952-01-17', '688-566566', '17-donato.jpg', TRUE, 10, 10);  		-- aceptado por admin en request 10  (socio de AA)
	
-- 7.3. Clubs
-- Derivados de las requests aceptadas anteriores (5 clubes)
-- Clubes empiezan sus iDS en 100
INSERT INTO Club (idclub, name, description, sport, email, cp, city, photo, active, aud_updated_by, Request_idrequest) 
VALUES 
	(100, 'Club AA', 'Club de running', 'run', 'info@clubaa.com', '35000', 'Las Palmas', '100-run.jpg', TRUE, 2, 1),    			-- Club AA aceptado por Admin en Req=1
	(101, 'Club BB', 'Club de baloncesto', 'basket', 'info@clubbb.com', '35000', 'Las Palmas', '101-basket.jpg', TRUE, 2, 2),    	-- Club BB aceptado por Admin en Req=2
	(102, 'Club CC', 'Club de natación', 'swin', 'info@clubcc.com', '38000', 'Tenerife', '102-swim.jpg', TRUE, 2, 3),    			-- Club CC aceptado por Admin en Req=3
	(103, 'Club DD', 'Club de senderismo', 'hike', 'info@clubdd.com', '35000', 'Las Palmas', '103-hike.jpg', TRUE, 2, 4),    		-- Club DD aceptado por Admin en Req=4
	(104, 'Club EE', 'Club de trail', 'trail', 'info@clubee.com', '38350', 'Tacoronte', '104-trail.jpg', TRUE, 2, 5),    			-- Club EE aceptado por Admin en Req=5
	(105, 'Club FF', 'Club de balonmano', 'handball', 'info@club.com', '38003', 'Ifara', '105-handball.jpg', FALSE, 2, 6); 			-- Club FF aceptado por Admin en Req=6  (ya de baja, no tuvo socios, solo al gestor)
	
-- 7.4. Socios (relación User_Club). Tabla "pertenece_a"
-- Derivados de las requests aceptadas anteriores. Aquí empieza la magia de relaciones cruzadas
-- 
INSERT INTO pertenece_a (Users_iduser, Club_idclub, rol, unsuscribed_at)
VALUES 
	(10, 100, 'manager', NULL),				-- socios gestores de distintos clubs
	(11, 101, 'manager', NULL),
	(12, 102, 'manager', NULL),
	(13, 103, 'manager', NULL),	
	(14, 104, 'manager', NULL),
	(15, 105, 'manager', NULL),
	(16, 100, 'partner', NULL),				-- socio del club AA
	(17, 100, 'partner', NULL),				-- socio de tres clubes (uno de ellos de baja)
	(17, 101, 'partner', NULL);
/*	(17, 105, 'partner', NULL),				-- esta tupla de relación no debe existir, porque cuando se dio de baja al club se debió eliminar la relacion */
	
-- 7.5. Actividades
-- 5 Actividades exclusivas del Club AA (ID 100) - IDs empiezan en 1000
INSERT INTO Actividad (idactividad, title, description, sport, fecha, place, distancia, photo, Club_idclub)
VALUES
	(1000, 'Ruta Nocturna Las Palmas', 'Ruta nocturna urbana de 10km', 'run', '2023-11-15 19:00:00', 'Parque Santa Catalina', 10, '1000-nigthrun.jpg', 100),
	(1001, 'Maratón Anual Club AA', 'Maratón oficial con recorrido por la ciudad', 'run', '2023-12-10 08:00:00', 'Estadio de Gran Canaria', 42, '1001-annualmarathon.jpg', 100),
	(1002, 'Carrera de la Amistad', 'Carrera popular sin competitividad', 'run', '2023-12-24 10:00:00', 'Playa de Las Canteras', 5, '1002-friendlyrunners.jpg', 100),
	(1003, 'Trail Montaña AA', 'Trail de montaña de media dificultad', 'trail', '2024-01-15 07:00:00', 'Pico de las Nieves', 21, '1003-mountains.jpg', 100),
	(1004, 'Entrenamiento Intervalos', 'Sesión intensiva de intervalos', 'run', '2023-11-20 18:30:00', 'Pista de Atletismo', 0, '1004-intervals.jpg', 100);
    
-- 4 actividades exclusivas del Club BB (ID 101) - IDs a continuación
INSERT INTO Actividad (idactividad, title, description, sport, fecha, place, distancia, photo, Club_idclub)
VALUES
	(1005, 'Partido Liga Local', 'Partido de liga contra equipo local', 'basket', '2024-02-10 18:00:00', 'Pabellón Municipal Sur', NULL, '1005-liga.jpg', 101),
	(1006, 'Entrenamiento Tiros', 'Sesión de práctica de tiros libres y triples', 'basket', '2024-02-15 19:00:00', 'Canchas dde Ravelo', NULL, '1006-tiros.jpg', 101),
	(1007, 'Partido femenino', 'Partido de liga de nuestra división femenina', 'basket', '2024-07-12 18:00:00', 'Pabellón Municipal Sur', NULL, '1007-femenino.jpg', 101),
	(1008, 'Ver NBA', 'Reunión social para ver la NBA', 'basket', '2024-10-11 21:00:00', 'Local social del Club', NULL, '1008-vernba.jpg', 101);

-- 7.6. Publicaciones
-- 5 Publicaciones del Club AA (ID 100) - IDs empiezan en 1000
INSERT INTO Publicacion (idpublicacion, subject, text, photo, Club_idclub)
VALUES
	(1000, 'Bienvenida Temporada 2024', 'Os damos la bienvenida a la nueva temporada de running. ¡Inscripciones abiertas!', '1000-newsseason.png', 100),
	(1001, 'Normas del Club', 'Recordad las normas básicas de convivencia y seguridad en las salidas.', '1001-clubregulations.jpg', 100),
	(1002, 'Nuevo Material Disponible', 'Ya está disponible la nueva equipación en la tienda del club.', '1002-equipment.jpg', 100),
	(1003, 'Resultados Maratón', 'Publicación de los resultados de la última maratón celebrada.', '1003-results.jpg', 100),
	(1004, 'Asamblea General', 'Convocatoria de asamblea general de socios para el próximo viernes.', '1004-assembly.jpg', 100);
    
-- 4 publicaciones exclusivas del Club BB (ID 101) - IDs a continuación
INSERT INTO Publicacion (idpublicacion, subject, text, photo, Club_idclub)
VALUES
	(1005, 'Inicio Temporada 2024', 'Bienvenidos a la nueva temporada de baloncesto. ¡Inscripciones abiertas!', '1005-inicio.jpg', 101),
	(1006, 'Nueva Web', 'Ya está dispobible nuestra nueva Web', '1006-newweb.jpg', 101),
	(1007, 'Propuestas estatutos', 'Se abre el plazo de propuestas para los nuevos estatutos a revisar', '1007-propuestas.jpg', 101),
	(1008, 'Noticias semanales', 'Botetín de noticias semanales ya disponible en nuestro local social', '1008-sportnews.jpg', 101);
    
-- 7.7. Productos
-- 5 Productos exclusivos del Club AA (ID 100) - IDs empiezan en 1000
INSERT INTO Producto (idproducto, name, description, photo, precio, stock, Club_idclub)
VALUES
	(1000, 'Camiseta Oficial Club AA', 'Camiseta técnica de running edición 2024', '1000-tshirt.jpg', 25.50, 100, 100),
	(1001, 'Zapatillas ProRun AA', 'Zapatillas de asfalto edición limitada club', '1001-shoes.jpg', 120.00, 25, 100),
	(1002, 'Malla larga invierno', 'Pantalón running transpirable', '1002-pants.jpg', 18.00, 50, 100),
	(1003, 'Gorro Invierno AA', 'Gorro térmico con logo bordado', '1003-headcover.jpg', 12.50, 40, 100),
	(1004, 'Mochila Hidratación', 'Mochila ligera con depósito de agua', '1004-camelbag.jpg', 45.00, 15, 100);
    
-- 4 productos exclusivos del Club BB (ID 101) - IDs a continuación
INSERT INTO Producto (idproducto, name, description, photo, precio, stock, Club_idclub)
VALUES
	(1005, 'Equipación Club', 'Camiseta oficial de juego femenina del club', '1005-equipacionfem.jpg', 50.00, 50, 101),
	(1006, 'Balón Oficial', 'Balón de baloncesto tamaño oficial marca Spalding', '1006-balonspalding.jpg', 25.00, 20, 101),
	(1007, 'Set cintas', 'Set de ciontas del pelo de alta sujección', '1007-cintapelo.jpg', 10.00, 40, 101),
	(1008, 'Zapatilla Air Jordan', 'Zapatilla Air Jordan Vintage blanca y negra', '1008-airjordan.jpg', 60.00, 20, 101);

-- 7.8. Inscripciones (se_inscribe)
-- Inscripciones de los usuarios del club AA: Roberto (id=16), Donato (id=17) y Gestor AA (id=10) en las actividades antes creadas
INSERT INTO se_inscribe (Users_iduser, Actividad_idactividad)
VALUES
	(16, 1000), 		-- Roberto en Ruta Nocturna
	(16, 1001), 		-- Roberto en Maratón
	(16, 1002), 		-- Roberto en Carrera Amistad
	(16, 1003), 		-- Roberto en Trail
	(16, 1004), 		-- Roberto en Intervalos
	(17, 1000), 		-- Donato en Ruta Nocturna
	(17, 1001), 		-- Donato en Maratón
	(17, 1002), 		-- Donato en Carrera Amistad
	(10, 1001), 		-- Gestor en Maratón
	(10, 1004); 		-- Gestor en Intervalos

-- 7.9. Compras
-- Compras realizadas por los usuarios del Club AA en productos del Club AA
-- Nota: Producto_idproducto ahora referencia a los nuevos IDs (1000-1004)
INSERT INTO compra (idcompra, Users_iduser, Producto_idproducto, cantidad, total, estado, aud_updated_by)
VALUES
	-- Roberto compra varios items
	(1, 16, 1000, 2, 51.00, 'paid', 16),       			-- Roberto compra 2 Camisetas
	(2, 16, 1001, 1, 120.00, 'paid', 16),      			-- Roberto compra Zapatillas
	(3, 16, 1003, 1, 12.50, 'paid', 16),       			-- Roberto compra Gorro
	(4, 16, 1004, 1, 45.00, 'collected', 16),  			-- Roberto compra Mochila (ya recogida)
	-- Donato compra otros items
	(5, 17, 1000, 1, 25.50, 'paid', 17),       			-- Donato compra Camiseta
	(6, 17, 1002, 2, 36.00, 'paid', 17),       			-- Donato compra 2 Pantalones
	(7, 17, 1001, 1, 120.00, 'reserved', 17),  			-- Donato reserva Zapatillas
	(8, 17, 1003, 3, 37.50, 'paid', 17),       			-- Donato compra 3 Gorros
	-- El Gestor también hace compras para el club
	(9, 10, 1000, 10, 255.00, 'paid', 10),     			-- Gestor compra 10 Camisetas (stock)
	(10, 10, 1002, 5, 90.00, 'collected', 10); 			-- Gestor compra 5 Pantalones


-- Reactivamos chequeos de FK y reseteamos SQL Mode
SET SESSION sql_mode = DEFAULT;
SET FOREIGN_KEY_CHECKS = 1;