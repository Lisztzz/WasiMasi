# WasiMasi — Backend API

Backend del proyecto **WasiMasi** (*wasi* = casa, *masi* = compañero), una plataforma orientada a conectar a estudiantes universitarios que buscan u ofrecen habitaciones compartidas en Lima Metropolitana de forma segura, verificada y centralizada.

---

## 🛠️ Tecnologías y Herramientas

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot 3.x
* **Acceso a datos:** Spring Data JPA / Hibernate
* **Base de datos:** PostgreSQL / H2 (según configuración en entorno)


* **Gestor de dependencias:** Maven
* **Documentación & Pruebas API:** Postman

---

## 📁 Estructura del Proyecto

```text
WasiMasi/
├── backend/
│   ├── pom.xml
│   ├── README.md
│   └── src/main/
│       ├── java/com/wasimasi/api/
│       │   ├── controller/      # Endpoints REST (Auth, Publicaciones, Usuarios)
│       │   ├── dto/             # Objetos de transferencia de datos (Request/Response)
│       │   ├── exception/       # Manejador global y excepciones personalizadas
│       │   ├── mapper/          # Mapeo entre entidades de dominio y DTOs
│       │   ├── model/           # Entidades JPA y Enums de dominio
│       │   ├── repository/      # Interfaces de persistencia (Spring Data JPA)
│       │   ├── service/         # Lógica de negocio y casos de uso
│       │   └── WasimasiApiApplication.java
│       └── resources/
│           ├── application.yml  # Configuración del entorno y base de datos
│           └── data.sql         # Datos semilla iniciales
├── postman/
│   └── wasimasi.postman_collection.json # Colección de endpoints para testing
└── README.md

```

---

## 🚀 Módulos y Funcionalidades Principales

### 1. Autenticación y Cuentas (`AuthController`)

* **Registro de usuarios (`POST /api/v1/auth/register`):** Soporta flujos diferenciados para **Buscadores** (mediante validación de correo institucional y código universitario) y **Anfitriones** (con DNI).


* **Inicio de sesión (`POST /api/v1/auth/login`):** Validación de credenciales y retorno del perfil de usuario autenticado.



### 2. Gestión de Usuarios y Perfiles (`UsuarioController`)

* Consulta y actualización de datos de perfil.


* Control de estados de verificación (`EstadoVerificacion`: `PENDIENTE`, `VERIFICADO`, `RECHAZADO`).


* Diferenciación de roles (`RolUsuario`: `BUSCADOR`, `ANFITRION`).



### 3. Publicación de Habitaciones (`PublicacionController`)

* **Publicación:** Registro de habitaciones con título, descripción, precio, distrito/zona y fotos.


* **Búsqueda y filtros:** Consulta de ofertas activas filtrando por zona y presupuesto.


* **Ciclo de vida:** Transición de estados (`EstadoPublicacion`: `ACTIVA`, `PAUSADA`, `OCUPADA`).



---

## ⚙️ Requisitos Previos

* **JDK 17** o superior instalado.
* **Maven 3.8+** configurado en el `PATH`.
* **PostgreSQL** instalado y ejecutándose localmente (o configuración de contenedor Docker).

---

## 🔧 Instalación y Despliegue Local

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-organizacion/wasimasi.git
cd WasiMasi/backend

```

### 2. Configurar variables de base de datos

Revisa y edita el archivo `src/main/resources/application.yml` con tus credenciales locales:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wasimasi_db
    username: tu_usuario
    password: tu_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

```

### 3. Compilar el proyecto

```bash
mvn clean install

```

### 4. Ejecutar la aplicación

```bash
mvn spring-boot:run

```

La API estará disponible por defecto en: `http://localhost:8080`

---

## 🧪 Pruebas con Postman

Se incluye una colección lista para importar en el directorio `postman/`:

1. Abre **Postman**.
2. Haz clic en **Import** y selecciona el archivo `postman/wasimasi.postman_collection.json`.
3. Configura la variable de entorno `baseUrl` con el valor `http://localhost:8080`.
4. Ejecuta las peticiones de registro, login y gestión de habitaciones.

---

## 👥 Equipo de Desarrollo (Grupo WasiMasi - UPC)

* **Luis Enrique Aguilar Benites** (U202219719)


* **Piero Marcos Contreras Albornoz** (U202217724)


* **Ivan Antonio Almeida Aguilar** (U20231B249)


* **Sandro Lu Lock Figueroa Dextre** (U202315077)


* **Diego Alexander Huaman Sirio** (U202111F983)
