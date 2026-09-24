# WasiMasi API

Spring Boot 4.1 · Java 21 · Spring Data JPA · MapStruct · Lombok · PostgreSQL (local).

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

## Endpoints (primera version)

| Metodo | Ruta | US | Descripcion |
|---|---|---|---|
| POST | `/api/auth/registro` | US01 | Registrar buscador o anfitrion |
| POST | `/api/auth/login` | US02 | Iniciar sesion |
| GET | `/api/usuarios/{id}` | US05 | Ver un usuario y su estado de verificacion |
| POST | `/api/usuarios/{id}/simular-verificacion` | US03/US04 | Simula que el usuario quedo verificado |
| POST | `/api/publicaciones` | US06 | Publicar una habitacion |
| GET | `/api/publicaciones?zona=&precioMin=&precioMax=` | US08 | Buscar por zona y presupuesto |
| GET | `/api/publicaciones/{id}` | US11 | Ver el detalle de una publicacion |

## Reglas destacadas
- Correo duplicado en el registro → 409.
- Registro como `BUSCADOR` con correo que no sea de un dominio institucional
  reconocido (`upc.edu.pe`, `pucp.edu.pe`, `unmsm.edu.pe` por ahora) → 400.
- Login con credenciales invalidas → 401; si el usuario aun esta `PENDIENTE`
  de verificacion, el login igual funciona pero devuelve `requiereVerificacion: true`.
- Publicar sin fotos → 400; publicar con un anfitrion aun no verificado no
  falla, pero la publicacion se guarda como `BORRADOR` con una advertencia.
- Buscar con presupuesto minimo mayor al maximo → 400.
- Errores en formato JSON estandar: `{timestamp, status, error, message}`.
