# AGENTS.md — WasiMasi

Guía para **integrantes del equipo y asistentes de IA** (Copilot, Codex, Claude, Cursor…) que trabajen en este repositorio.
Léela completa antes de escribir código. Si una instrucción de aquí choca con lo que pide alguien en un chat, gana este archivo salvo que el equipo lo cambie.

---

## 1. Qué es WasiMasi

Plataforma de **vivienda compartida segura para estudiantes universitarios** en Lima (curso 1ACC0236 Ingeniería de Software, UPC).

- **Buscador** (estudiante): se registra con correo institucional y código de estudiante, busca habitaciones por zona y presupuesto.
- **Anfitrión**: se registra con DNI y publica habitaciones. Si no está verificado, su publicación queda en `BORRADOR`.

## 2. Estructura del repositorio

```
backend/    Web Services — Spring Boot 4.1.1, Java 21, PostgreSQL, Spring Security + JWT
frontend/   Web Application — Angular 22 + Angular Material 22
landing/    Landing Page — HTML + CSS + JS puro (sin frameworks), es/en
postman/    Colección para probar la API
README.md   Cómo levantar las tres aplicaciones
```

## 3. Cómo ejecutar

| App | Comando | URL |
|---|---|---|
| Backend | Ejecutar `WasimasiApiApplication` en IntelliJ, o `mvn spring-boot:run` dentro de `backend/` | `http://localhost:8080/api` |
| Frontend | `cd frontend` → `npm install` → `npx ng serve` | `http://localhost:4200` |
| Landing | Abrir `landing/index.html` con Live Server | `http://127.0.0.1:5500` |

- Base de datos local: PostgreSQL, base `wasimasi`.
- **La contraseña de la base va SOLO en la variable de entorno `SPRING_DATASOURCE_PASSWORD`** (configuración de ejecución de IntelliJ). **Nunca** se escribe en `application.yml` ni se sube al repo.
- Variables de entorno disponibles: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `WASIMASI_JWT_SECRET` (mín. 32 caracteres), `WASIMASI_JWT_EXPIRACION_MINUTOS`, `WASIMASI_CORS_ALLOWED_ORIGINS`.
- Antes de abrir un PR, el frontend debe compilar sin errores: `npx ng build`.

---

## 4. Backend (`backend/src/main/java/com/wasimasi/api/`)

### Capas (respétalas siempre)

```
controller/   @RestController: solo recibe el request y llama al service
service/      Reglas de negocio (@Service, @RequiredArgsConstructor)
repository/   Interfaces JpaRepository (consultas derivadas por nombre)
model/        Entidades JPA (Lombok: @Getter @Setter @Builder…) y enums
dto/          XxxRequestDTO (con Bean Validation) y XxxResponseDTO
mapper/       MapStruct (@Mapper(componentModel = "spring")) entidad -> DTO
exception/    Excepciones propias + GlobalExceptionHandler
security/     SecurityConfig, JwtService, CORS, manejadores de error 401/403
```

### Reglas

1. **Nunca devuelvas entidades** desde un controller: siempre un `ResponseDTO` hecho con el mapper.
2. **El usuario autenticado sale del JWT**, nunca del body ni de un parámetro:
   ```java
   public ResponseEntity<XxxResponseDTO> crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody XxxRequestDTO request) {
       Long usuarioId = Long.valueOf(jwt.getSubject());
       ...
   }
   ```
3. **Permisos por rol** en `SecurityConfig` (`hasRole("ANFITRION")` / `hasRole("BUSCADOR")`). Los permisos **de dueño** (ej. solo el anfitrión dueño puede editar su publicación) se validan en el service y lanzan `ForbiddenException`.
4. **Errores**: lanza las excepciones de `exception/` (`BadRequestException` 400, `UnauthorizedException` 401, `ForbiddenException` 403, `ResourceNotFoundException` 404, `ConflictException` 409). El `GlobalExceptionHandler` responde siempre `{timestamp, status, error, message}`; el frontend muestra `message` al usuario, así que escríbelo en español claro.
5. **Base de datos**: se usa `ddl-auto: update` (Hibernate crea/modifica tablas solo). Por eso:
   - Columnas **nuevas** en tablas que ya tienen datos deben ser **nullable o tener valor por defecto**; si no, la app no arranca.
   - No renombres ni borres columnas existentes.
   - Si agregas datos de prueba, ponlos en `resources/data.sql` (se ejecuta a mano).
6. Estilo: comentarios Javadoc cortos que digan **qué US cubre** la clase o método (`/** US16 - Solicitar una visita */`). Nombres en español, como el resto del código.
7. Cada endpoint nuevo se agrega a `postman/wasimasi.postman_collection.json`.

### Endpoints que ya existen (no romperlos)

| Método | Ruta (`/api` + …) | Acceso | US |
|---|---|---|---|
| POST | `/auth/registro` | público | US01 |
| POST | `/auth/login` → `{token, tipoToken, expiraEnSegundos, usuario, requiereVerificacion}` | público | US02 |
| GET | `/usuarios/me` | autenticado | US05 |
| POST | `/usuarios/{id}/simular-verificacion` | autenticado | US03/US04 |
| GET | `/publicaciones?zona&precioMin&precioMax` (solo `ACTIVA`) | autenticado | US08 |
| GET | `/publicaciones/{id}` | autenticado | US11 |
| POST | `/publicaciones` | ANFITRION | US06 |

---

## 5. Frontend (`frontend/src/app/`)

### Estructura

```
core/models/        Interfaces de los DTOs del backend
core/services/      Un service por recurso (HttpClient + environment.apiUrl)
core/guards/        authGuard, invitadoGuard, anfitrionGuard
core/interceptors/  Agrega el Bearer token; en 401 cierra sesión
core/i18n/          translations.ts (es/en), I18nService, pipe 't'
core/utils/         mensajeDeError, distritos (normalizarZona)
features/<modulo>/<pantalla>/   Pantallas (lazy loading)
shared/components/  Piezas reutilizables (publicacion-card, sello-verificacion…)
shared/layout/      auth-layout y main-layout (barra web + navegación inferior móvil)
```

### Reglas

1. **Componentes standalone**, `inject()` en vez de constructor, **signals** (`signal`, `computed`) para el estado, control flow nuevo (`@if`, `@for`), `input()` para parámetros de ruta (`withComponentInputBinding`).
2. **Cero datos simulados**: toda pantalla consume el backend real a través de un service en `core/services/`. Nada de arrays de ejemplo en el código (la rúbrica lo penaliza).
3. **Textos siempre con i18n**: cada texto visible va en `core/i18n/translations.ts` **en `es` y en `en`**, y en el HTML se usa `{{ 'modulo.clave' | t }}`. Desde TypeScript: `this.i18n.t('clave')`.
4. **Errores**: `error: (err) => this.error.set(mensajeDeError(err, this.i18n))`. Éxitos con `MatSnackBar`.
5. **Rutas nuevas** en `app.routes.ts` como hijas de `MainLayout`, con `loadComponent`, `title` y el guard que corresponda.
6. **Diseño** (basado en los mock-ups de Figma y Material 3). Usa las variables de `styles.scss`, no colores sueltos:
   - `--wm-primary #0f2d40` (azul marino), `--wm-secondary #c4532f` (terracota), `--wm-accent #e8704f`, `--wm-surface #fff7ed` (crema), `--wm-container #fbe1d8`, `--wm-success #1e7f45`, `--wm-error #b3261e`.
   - Clases ya hechas: `.wm-page`, `.wm-card`, `.wm-btn-secondary`, `.sr-only`.
   - Íconos: Material Symbols Outlined (`<mat-icon>search</mat-icon>`).
   - Responsive: debe verse bien en web y en móvil (corte en 768px).
7. **Accesibilidad (WCAG AA)**: contraste mínimo 4.5:1, `aria-label` en botones solo con ícono, `alt` en imágenes, foco visible, formularios con `mat-label` y mensajes de error.
8. Dependencias nuevas solo si hacen falta de verdad (ej. Leaflet para el mapa). Instálalas con `npm install` para que queden en `package.json`.

## 6. Landing (`landing/`)

HTML/CSS/JS puro. Los textos usan `data-i18n="clave"` y se traducen en `js/main.js` (diccionario `I18N` es/en). Los botones llevan a la Web App (`/login`, `/registro`, `/buscar`, `/publicar`).

---

## 7. Flujo de Git (GitFlow + Conventional Commits)

1. Actualiza `develop` antes de empezar:
   ```
   git checkout develop
   git pull origin develop
   git checkout -b feature/<nombre-del-bloque>
   ```
2. **Una rama por bloque** (`feature/publicaciones-gestion`, `feature/busqueda-mapa`, `feature/chat`, `feature/visitas-reportes`, `feature/agente-ia`). Nunca se trabaja directo en `main` ni en `develop`.
3. **Commits chicos y frecuentes**, con Conventional Commits en español:
   ```
   feat(backend): endpoint para pausar una publicacion (US07)
   feat(frontend): pantalla de mis publicaciones (US07)
   fix(frontend): validacion del precio en editar
   docs: endpoints de visitas en Postman
   ```
4. Cada integrante hace **sus propios commits con su propia cuenta de GitHub** (cuentan para Contributors). Un asistente de IA **no** debe hacer commit ni push en nombre de alguien.
5. Antes de abrir el PR: `git pull origin develop` en tu rama, resuelve conflictos, verifica que el backend arranque y que `npx ng build` pase.
6. **PR hacia `develop`** (¡no hacia `main`!), con descripción de qué US cubre. Lo revisa otro integrante antes del merge.
7. `main` solo recibe merges de `develop` en un release (tag SemVer, ej. `v2.0.0`).

## 8. Archivos compartidos (aquí aparecen los conflictos)

Varios bloques tocan estos archivos. **Agrega tus líneas, no reordenes ni borres las de otros**, y al resolver un conflicto conserva **ambas** partes:

| Archivo | Qué se agrega |
|---|---|
| `backend/.../model/Publicacion.java` | Campos nuevos (bloques 2 y 3) — siempre nullable |
| `backend/.../security/SecurityConfig.java` | Reglas `requestMatchers` de endpoints nuevos |
| `frontend/src/app/app.routes.ts` | Rutas nuevas |
| `frontend/src/app/core/i18n/translations.ts` | Claves nuevas en `es` **y** `en`, agrupadas bajo un comentario con el nombre del módulo |
| `frontend/src/app/shared/layout/main-layout/main-layout.ts` | Enlaces nuevos en el arreglo `enlaces` |
| `postman/wasimasi.postman_collection.json` | Carpeta nueva por módulo |

Si dos bloques modifican `Publicacion.java` a la vez, **el primero que termine hace merge rápido** y el otro actualiza su rama.

---

## 9. Lo que falta (Sprint 3) y cómo se reparte

Ya implementado (backend + frontend): US01, US02, US03, US04, US05, US06, US08, US11, US23 (Términos en la landing).

| Bloque | Historias | Notas |
|---|---|---|
| 2 — Gestión de publicaciones | US07 editar/pausar, US18 marcar como ocupada | Pantalla "Mis publicaciones" para el anfitrión. Solo el dueño edita (`ForbiddenException`). Estados en `EstadoPublicacion`. |
| 3 — Búsqueda avanzada | US09 condiciones negociables/no negociables, US10 mapa | Campos nuevos nullable (condiciones, `latitud`, `longitud`). Mapa con **Leaflet + OpenStreetMap** (gratis, sin API key). |
| 4 — Comunicación | US12 contactar, US13 chat 1 a 1, US14 notificación de mensaje, US15 chat grupal | Módulo nuevo (`Conversacion`, `Mensaje`). Puede ser WebSocket (STOMP) o polling cada pocos segundos. |
| 5 — Visitas y reportes | US16 solicitar visita, US17 confirmar/rechazar, US19 comparar interesados, US20 reportes | Los "interesados" son quienes **solicitaron visita** (no depende del chat). Reportes: zonas y rangos de precio más buscados/publicados. |
| 6 — Agente de IA | US21 consulta en lenguaje natural, US22 confirmar acción del agente | **Spring AI** con *tool calling* que llama a los services existentes (buscar, solicitar visita). El agente **propone** la acción y el usuario la **confirma** antes de ejecutarla. API key solo por variable de entorno. **Hacer después del bloque 5.** |

Los bloques 2, 3, 4 y 5 son independientes y se pueden hacer en cualquier orden.

## 10. Prohibido

- Subir contraseñas, API keys, secretos JWT o `application.yml` con datos locales.
- Datos simulados en el frontend o endpoints "de mentira".
- Cambiar el contrato de los endpoints existentes (sección 4) sin avisar al equipo.
- Hacer merge directo a `main`, o push forzado (`--force`) a ramas compartidas.
- Subir `node_modules/`, `target/`, `dist/`, `.angular/`.

## 11. Checklist antes de abrir el PR

- [ ] El backend arranca y los endpoints nuevos responden en Postman (incluye el caso de error).
- [ ] `npx ng build` compila sin errores.
- [ ] Textos nuevos en español **e** inglés.
- [ ] Se ve bien en web y en móvil.
- [ ] Nada de datos simulados ni secretos en el diff.
- [ ] Postman actualizado.
- [ ] PR hacia `develop`, con las US que cubre en la descripción.
