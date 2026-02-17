CREATE TABLE persona (
  dni VARCHAR2(9) NOT NULL,
  nombre VARCHAR2(100) NOT NULL,
  apellidos VARCHAR2(100) NOT NULL,
  direccion VARCHAR2(255) NOT NULL,
  cp VARCHAR2(10) NOT NULL,
  CONSTRAINT pk_persona PRIMARY KEY (dni)
);

CREATE TABLE peticion (
  id_peticion NUMBER(10) NOT NULL,
  tipo VARCHAR2(100) NOT NULL,
  estado VARCHAR2(100) NOT NULL,
  dni_peticionario VARCHAR2(9) NOT NULL,
  id_club_destino NUMBER(10) NOT NULL,
  email_validador VARCHAR2(254),
  CONSTRAINT pk_peticion PRIMARY KEY (id_peticion)
);

CREATE TABLE usuario (
  email VARCHAR2(254) NOT NULL,
  contrasena VARCHAR2(255) NOT NULL,
  estado VARCHAR2(100) NOT NULL,
  CONSTRAINT pk_usuario PRIMARY KEY (email)
);

CREATE TABLE club (
  id_club NUMBER(10) NOT NULL,
  nombre VARCHAR2(100) NOT NULL,
  direccion VARCHAR2(255) NOT NULL,
  cp VARCHAR2(10) NOT NULL,
  deporte VARCHAR2(100) NOT NULL,
  estado VARCHAR2(100) NOT NULL,
  imagen VARCHAR2(4000),
  CONSTRAINT pk_club PRIMARY KEY (id_club)
);

CREATE TABLE actividad (
  id_actividad NUMBER(10) NOT NULL,
  titulo VARCHAR2(100) NOT NULL,
  descripcion CLOB NOT NULL,
  modalidad VARCHAR2(100),
  fecha DATE NOT NULL,
  inicio VARCHAR2(5) NOT NULL,
  distancia VARCHAR2(255),
  id_club NUMBER(10) NOT NULL,
  CONSTRAINT pk_actividad PRIMARY KEY (id_actividad)
);

CREATE TABLE publicacion (
  id_publicacion NUMBER(10) NOT NULL,
  titulo VARCHAR2(100) NOT NULL,
  texto CLOB NOT NULL,
  imagen VARCHAR2(4000),
  id_club NUMBER(10) NOT NULL,
  CONSTRAINT pk_publicacion PRIMARY KEY (id_publicacion)
);

CREATE TABLE producto (
  id_producto NUMBER(10) NOT NULL,
  tangible CHAR(1) NOT NULL,
  nombre VARCHAR2(100) NOT NULL,
  descripcion CLOB,
  precio NUMBER(10,2) NOT NULL,
  id_club NUMBER(10) NOT NULL,
  CONSTRAINT pk_producto PRIMARY KEY (id_producto)
);

CREATE TABLE ticket (
  id_ticket NUMBER(10) NOT NULL,
  asunto VARCHAR2(100) NOT NULL,
  descripcion CLOB NOT NULL,
  prioridad VARCHAR2(100),
  estado VARCHAR2(100) NOT NULL,
  dni_creador VARCHAR2(9) NOT NULL,
  email_atendedor VARCHAR2(254),
  CONSTRAINT pk_ticket PRIMARY KEY (id_ticket)
);

CREATE TABLE usuario_club (
  email VARCHAR2(254) NOT NULL,
  id_club NUMBER(10) NOT NULL,
  rol VARCHAR2(100) NOT NULL,
  CONSTRAINT pk_usuario_club PRIMARY KEY (email, id_club)
);

CREATE TABLE inscripcion (
  email VARCHAR2(254) NOT NULL,
  id_actividad NUMBER(10) NOT NULL,
  CONSTRAINT pk_inscripcion PRIMARY KEY (email, id_actividad)
);

CREATE TABLE compra (
  id_compra NUMBER(10) NOT NULL,
  email VARCHAR2(254) NOT NULL,
  id_producto NUMBER(10) NOT NULL,
  cantidad NUMBER(10) NOT NULL,
  estado VARCHAR2(100) NOT NULL,
  CONSTRAINT pk_compra PRIMARY KEY (id_compra)
);

ALTER TABLE peticion ADD CONSTRAINT fk_peticion_dni_peticionario FOREIGN KEY (dni_peticionario) REFERENCES persona(dni);
ALTER TABLE peticion ADD CONSTRAINT fk_peticion_id_club_destino FOREIGN KEY (id_club_destino) REFERENCES club(id_club);
ALTER TABLE peticion ADD CONSTRAINT fk_peticion_email_validador FOREIGN KEY (email_validador) REFERENCES usuario(email);
ALTER TABLE actividad ADD CONSTRAINT fk_actividad_id_club FOREIGN KEY (id_club) REFERENCES club(id_club);
ALTER TABLE publicacion ADD CONSTRAINT fk_publicacion_id_club FOREIGN KEY (id_club) REFERENCES club(id_club);
ALTER TABLE producto ADD CONSTRAINT fk_producto_id_club FOREIGN KEY (id_club) REFERENCES club(id_club);
ALTER TABLE ticket ADD CONSTRAINT fk_ticket_dni_creador FOREIGN KEY (dni_creador) REFERENCES persona(dni);
ALTER TABLE ticket ADD CONSTRAINT fk_ticket_email_atendedor FOREIGN KEY (email_atendedor) REFERENCES usuario(email);
ALTER TABLE usuario_club ADD CONSTRAINT fk_usuario_club_email FOREIGN KEY (email) REFERENCES usuario(email);
ALTER TABLE usuario_club ADD CONSTRAINT fk_usuario_club_id_club FOREIGN KEY (id_club) REFERENCES club(id_club);
ALTER TABLE inscripcion ADD CONSTRAINT fk_inscripcion_email FOREIGN KEY (email) REFERENCES usuario(email);
ALTER TABLE inscripcion ADD CONSTRAINT fk_inscripcion_id_actividad FOREIGN KEY (id_actividad) REFERENCES actividad(id_actividad);
ALTER TABLE compra ADD CONSTRAINT fk_compra_email FOREIGN KEY (email) REFERENCES usuario(email);
ALTER TABLE compra ADD CONSTRAINT fk_compra_id_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto);