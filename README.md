# 🏃 TrackYours  
### Tu club, más participativo y social

![Java](https://img.shields.io/badge/Java-17_LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-brightgreen)
![BootStrap](https://img.shields.io/badge/BootStrap-5.x-purple)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Status](https://img.shields.io/badge/status-MVP_in_progress-danger)

Plataforma web para la gestión de clubes deportivos no profesionales.  
Desarrollada como **Producto Mínimo Viable (MVP)** con arquitectura MVC monolítica.

---

## 🚀 ¿Qué es TrackYours?

TrackYours es una aplicación web pensada para:

- 🏟 Gestionar clubes deportivos  
- 👥 Administrar socios  
- 🏃 Organizar actividades  
- 📰 Publicar anuncios  
- 🛍 Gestionar productos y cuotas  

Todo desde una interfaz simple, clara y responsiva.

---

## 👤 Roles del sistema

- **Administrador** → valida clubes  
- **Gestor de club** → gestiona contenidos y socios  
- **Socio** → participa en actividades y compras  
- **Visitante** → consulta información y solicita alta  

---

## 🧩 Funcionalidades principales

✔ Alta y validación de clubes  
✔ CRUD de actividades, publicaciones y productos  
✔ Inscripción a actividades  
✔ Gestión de socios  
✔ Tienda sin pasarela de pago (solo gestión interna)  
✔ Seguridad con autenticación y roles  

---

## 🏗 Arquitectura

```
MVC (Modelo - Vista - Controlador)
Arquitectura monolítica
Aplicación web tipo landing + espacio privado por club
```

---

## 🛠 Stack tecnológico

### Backend
- Java 17  
- Spring Boot  
- Spring Security  
- Spring Data JPA (Hibernate)  
- Maven  

### Frontend
- HTML5  
- CSS3  
- JavaScript (ES6+)  
- Bootstrap 5  
- Thymeleaf  

### Base de datos
- MySQL 8  

### Despliegue
- Docker  
- Tomcat  
- Linux (Ubuntu)  

---

## 🔐 Seguridad

- Autenticación con Spring Security  
- Contraseñas cifradas  
- Control de acceso por roles  
- Protección CSRF  

---

## ▶️ Cómo ejecutarlo

### 1️⃣ Clonar el repositorio
```bash
git clone https://github.com/TU_USUARIO/trackyours.git
cd trackyours
```

### 2️⃣ Ejecutar
```bash
mvn spring-boot:run
```

O instrucciones detalladas de despliegue en:

```bash
TrackYours/docs/deliverables/Documento de despliegue
```

Accede en:

```
http://localhost:8080/tracks
```

---

## 📌 Alcance

Este proyecto es un **MVP académico / PoC**.

No incluye:

- ❌ App móvil nativa  
- ❌ Microservicios  
- ❌ Integración con sistemas de pago  

---

## 👨‍💻 Autor

Roberto Himar Medina Sosa  
RHMS Sporting Developments  

---

> Proyecto enfocado a validar la viabilidad de una plataforma de gestión deportiva accesible, sencilla y escalable.
