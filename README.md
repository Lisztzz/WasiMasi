# WasiMasi

**WasiMasi** (*wasi* = casa, *masi* = compañero) conecta a estudiantes universitarios que buscan habitación en Lima
con anfitriones verificados por correo institucional o DNI.

Proyecto del curso 1ACC0236 Ingeniería de Software (UPC).

## Estructura del repositorio

| Carpeta | Qué contiene | Tecnologías |
| --- | --- | --- |
| [`backend/`](backend/README.md) | Web Services REST | Java 21, Spring Boot 4.1, Spring Security + JWT, Spring Data JPA, PostgreSQL |
| [`frontend/`](frontend/README.md) | Web App para buscadores y anfitriones | Angular 22, Angular Material |
| `landing/` | Landing Page v1 | HTML, CSS y JavaScript |
| `postman/` | Colección de pruebas de la API | Postman |
| `docs/` | Diagramas C4, de clases y de base de datos | PlantUML, Mermaid |

## Ejecutar todo en local

1. **Backend:** crear la base `wasimasi` en PostgreSQL y correr `WasimasiApiApplication` (detalle en `backend/README.md`). Queda en `http://localhost:8080/api`.
2. **Web App:** `cd frontend`, `npm install` y `npx ng serve`. Queda en `http://localhost:4200`.
3. **Landing Page:** abrir `landing/index.html` con Live Server. Queda en `http://127.0.0.1:5500`.

## Flujo de trabajo

- GitFlow: `main` (versiones entregadas), `develop` (integración) y ramas `feature/*`.
- Todo cambio entra por Pull Request hacia `develop`, revisado por otro integrante.
- Commits con *Conventional Commits* (`feat:`, `fix:`, `docs:`, `chore:`) y versiones con SemVer.

## Equipo

- Aguilar Benites, Luis Enrique (U202219719)
- Almeida Aguilar, Ivan Antonio (U20231B249)
- Contreras Albornoz, Piero Marcos (U202217724)
- Figueroa Dextre, Sandro Lu Lock (U202315077)
- Huaman Sirio, Diego Alexander (U202111F983)
