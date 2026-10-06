# WasiMasi API

Spring Boot 4.1 · Java 21 · Spring Data JPA · Spring Security + JWT · MapStruct · Lombok · PostgreSQL (local).

API del backend de WasiMasi. Esta primera version cubre el flujo minimo viable
priorizado en el Product Backlog: **US01** (registro), **US02** (login),
**US03/US04** (verificacion, **simulada** por ahora), **US06** (publicar
habitacion), **US08** (buscar por zona/presupuesto) y **US11** (ver detalle).

> La verificacion de correo institucional y de DNI todavia no tiene logica
> real (sin envio de correo ni validacion de documento): se simula con el
> endpoint `POST /usuarios/{id}/simular-verificacion`, que pasa al usuario a
> estado `VERIFICADO` directamente. Esto se reemplazara mas adelante por el
> flujo real.

## Requisitos
- JDK 21, Maven 3.9+
- PostgreSQL corriendo en local (por defecto se conecta a `localhost:5432/wasimasi`)

## Levantar en local
1. Crear la base de datos en tu PostgreSQL local:
   ```sql
   CREATE DATABASE wasimasi;
   ```
2. (Opcional) Si tu usuario/contrasena no son `postgres`/`postgres`, exporta:
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/wasimasi"
   export SPRING_DATASOURCE_USERNAME="<tu_usuario>"
   export SPRING_DATASOURCE_PASSWORD="<tu_password>"
   ```
   Cuando mas adelante quieran migrar a Supabase u otra base en la nube (como
   hizo el proyecto de ejemplo), basta con cambiar estas tres variables por el
   `JDBC URL` de Supabase; el resto del proyecto no cambia.
3. Ejecutar (con JDK 21):
   ```bash
   mvn spring-boot:run
   ```
   Hibernate crea las tablas (`ddl-auto: update`). La API queda en
   `http://localhost:8080/api`.
4. (Opcional) Cargar datos de prueba ejecutando una sola vez
   `src/main/resources/data.sql` contra tu base local (por ejemplo desde
   pgAdmin o `psql -d wasimasi -f src/main/resources/data.sql`), despues de
   haber levantado la app al menos una vez.

## Seguridad (autenticacion y autorizacion)
- **Autenticacion con JWT**: `POST /api/auth/login` devuelve un `token` (JWT
  firmado con HS256, expira en 120 min por defecto). Los demas endpoints
  exigen el header `Authorization: Bearer <token>`.
- **Contrasenas con BCrypt**: nunca se guardan en texto plano.
- **Autorizacion por rol**: el rol del usuario va en el token (claim `roles`).
  Solo un `ANFITRION` puede publicar; un `BUSCADOR` recibe 403.
- **Propiedad del recurso**: al publicar, el anfitrion se toma del token (no del
  body), y cada usuario solo puede simular la verificacion de su propia cuenta.
- La clave de firma se configura con la variable de entorno `WASIMASI_JWT_SECRET`
  (minimo 32 caracteres). Si no se define, se usa una clave de desarrollo.

## Endpoints (primera version)

| Metodo | Ruta | US | Acceso | Descripcion |
|---|---|---|---|---|
| POST | `/api/auth/registro` | US01 | Publico | Registrar buscador o anfitrion |
| POST | `/api/auth/login` | US02 | Publico | Iniciar sesion, devuelve JWT |
| GET | `/api/usuarios/me` | US05 | Autenticado | Datos del usuario del token |
| GET | `/api/usuarios/{id}` | US05 | Autenticado | Ver un usuario y su estado de verificacion |
| POST | `/api/usuarios/{id}/simular-verificacion` | US03/US04 | Solo el propio usuario | Simula que el usuario quedo verificado |
| POST | `/api/publicaciones` | US06 | Solo ANFITRION | Publicar una habitacion |
| GET | `/api/publicaciones?zona=&precioMin=&precioMax=` | US08 | Autenticado | Buscar por zona y presupuesto |
| GET | `/api/publicaciones/{id}` | US11 | Autenticado | Ver el detalle de una publicacion |

## Reglas destacadas
- Correo duplicado en el registro → 409.
- Registro como `BUSCADOR` con correo que no sea de un dominio institucional
  reconocido (`upc.edu.pe`, `pucp.edu.pe`, `unmsm.edu.pe` por ahora) → 400.
- Login con credenciales invalidas → 401; si el usuario aun esta `PENDIENTE`
  de verificacion, el login igual funciona pero devuelve `requiereVerificacion: true`.
- Publicar sin fotos → 400; publicar con un anfitrion aun no verificado no
  falla, pero la publicacion se guarda como `BORRADOR` con una advertencia.
- Buscar con presupuesto minimo mayor al maximo → 400.
- Sin token o con token invalido/expirado → 401; rol sin permiso o recurso ajeno → 403.
- Errores en formato JSON estandar: `{timestamp, status, error, message}`
  (incluidos los 401/403 de seguridad).

## Reiniciar los datos de prueba
Los usuarios creados antes de agregar BCrypt tienen la contrasena en texto
plano y ya no pueden iniciar sesion. Para empezar limpio (por ejemplo antes de
correr la coleccion de Postman), ejecutar en pgAdmin sobre la base `wasimasi`:
```sql
TRUNCATE usuarios, publicaciones, publicacion_fotos RESTART IDENTITY CASCADE;
```
