# WasiMasi — Web App (Angular)

Aplicación web de WasiMasi para buscadores y anfitriones de habitaciones compartidas en Lima.
Consume los Web Services del backend (`/backend`) con datos reales; no usa datos simulados.

Angular 22 · Angular Material (Material Design 3) · TypeScript · SCSS

## Requisitos

- Node.js 20.19+ (probado con Node 24) y npm
- Backend de WasiMasi corriendo en `http://localhost:8080/api` (ver `backend/README.md`)

## Ejecutar en local

```bash
cd frontend
npm install
npx ng serve
```

Abrir `http://localhost:4200`. La app recarga sola al guardar cambios.

La URL del backend está en `src/environments/` (`environment.development.ts` para `ng serve`).
El backend debe permitir el origen `http://localhost:4200` en CORS (ya configurado en `CorsConfig`).

Compilar para producción:

```bash
npx ng build
```

## Pantallas y User Stories

| Ruta | Pantalla | User Story | Endpoint |
| --- | --- | --- | --- |
| `/registro` | Registro de buscador o anfitrión | US01 | `POST /auth/registro` |
| `/login` | Inicio de sesión | US02 | `POST /auth/login` |
| `/perfil` | Perfil y verificación de identidad | US03, US04, US05 | `GET /usuarios/me`, `POST /usuarios/{id}/simular-verificacion` |
| `/buscar` | Búsqueda por zona y presupuesto | US08 | `GET /publicaciones?zona&precioMin&precioMax` |
| `/publicaciones/:id` | Detalle de la habitación | US11 | `GET /publicaciones/{id}` |
| `/publicar` | Publicar habitación (solo anfitriones) | US06 | `POST /publicaciones` |

## Estructura

```text
src/app/
├── core/
│   ├── guards/         # authGuard, invitadoGuard, anfitrionGuard
│   ├── i18n/           # Textos ES/EN, servicio de idioma y pipe "t"
│   ├── interceptors/   # Agrega "Authorization: Bearer <token>" y maneja el 401
│   ├── models/         # Interfaces que copian los DTO del backend
│   ├── services/       # AuthService, UsuarioService, PublicacionService
│   └── utils/          # Mensajes de error de la API y distritos de Lima
├── features/
│   ├── auth/           # Login y registro
│   ├── perfil/         # Perfil y verificación
│   └── publicaciones/  # Búsqueda, detalle y publicar
└── shared/
    ├── components/     # Logo, selector de idioma, tarjeta de habitación, sello de verificación
    └── layout/         # Plantilla de login/registro y plantilla con navegación
```

## Seguridad

- El JWT de `POST /auth/login` se guarda en el navegador y el interceptor lo envía en cada petición a la API.
- Si la API responde 401 (token vencido), se cierra la sesión y se vuelve al login.
- Las rutas internas exigen sesión (`authGuard`) y `/publicar` exige el rol ANFITRION (`anfitrionGuard`).

## Diseño, idioma y accesibilidad

- Guía de estilo y mock-ups en Figma: [WasiMasi — Guía de Estilo y Mock-ups (TB3)](https://www.figma.com/design/j5wYGizH0buhBJYvo6Lv9k/WasiMasi---Gu%C3%ADa-de-Estilo-y-Mock-ups--TB3-?node-id=13-105)
- Paleta: azul marino `#0F2D40`, naranja `#C4532F`, crema `#FFF7ED` (regla 60-30-10, contraste WCAG AA).
- Selector de idioma ES/EN en todas las pantallas; la elección se recuerda en el navegador.
- HTML semántico, enlace "Saltar al contenido", foco visible, `aria-label` en botones de solo ícono y textos alternativos en las fotos.
- Responsive: barra de navegación superior en web y navegación inferior en mobile (corte en 768 px).
