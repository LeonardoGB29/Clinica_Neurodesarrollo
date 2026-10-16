# clinica-backend

API REST del **Sistema de Gestión Clínica Interdisciplinaria** del Centro de Neurodesarrollo (TEA/TDAH) de la
Clínica San Juan de Dios. Implementa el contrato y el modelo de datos definidos en
[`../STACK_TECNOLOGICO.md`](../STACK_TECNOLOGICO.md) (secciones 4 y 6) y expone exactamente la misma forma de
datos que `clinica-frontend/src/data/mockData.js`, de modo que el front se conecta reemplazando el hook
`useClinicalRecord` por llamadas HTTP, sin tocar sus componentes.

## Stack

| Componente | Versión | Uso |
| --- | --- | --- |
| Java | 21 (LTS) | Lenguaje de la API. Se compila con `--release 21`; funciona también con un JDK más nuevo (probado con JDK 25). |
| Spring Boot | 3.5.16 | Web MVC, Data JPA (Hibernate 6.6), Validation, Security, Actuator. |
| Spring Security + jjwt | 6.5 / 0.13 | Autenticación sin estado con JWT firmado con HMAC (HS256/HS512 según la longitud de la clave) y autorización por rol. |
| PostgreSQL | 16 | Base de datos (contenedor `postgres:16-alpine`). |
| Flyway | 11 | `V1__esquema_inicial.sql` (esquema) y `V2__datos_semilla.sql` (datos de mockData.js). |
| MapStruct + Lombok | 1.6.3 / 1.18.48 | Mapeo entidad → DTO y reducción de código repetitivo. |
| springdoc-openapi | 2.8.17 | Swagger UI en `/swagger-ui.html`. |
| OpenPDF | 3.0.5 | Reporte PDF de la ficha de evolución. |
| JUnit 5, Mockito, Testcontainers | — | Pruebas unitarias y de integración contra PostgreSQL real. |
| Maven | 3.9 | Construcción (incluye *Maven Wrapper*: no hace falta instalar Maven). |

## Requisitos

- **JDK 21 o superior** (`java -version`).
- **Docker Desktop** (para PostgreSQL con Docker Compose y para las pruebas de integración).
- Maven es opcional: el proyecto incluye `mvnw` / `mvnw.cmd`, que descargan Maven 3.9.16 la primera vez.

## Puesta en marcha

1. **Levantar PostgreSQL 16** (crea la base `clinica`, usuario `clinica`, clave `clinica`, publicada en el
   puerto **5433** del host para no chocar con otras instancias de PostgreSQL que usen 5432):

   ```bash
   docker compose up -d db
   ```

   Para usar otro puerto: `CLINICA_DB_PORT=5444 docker compose up -d db` y `DB_URL=jdbc:postgresql://localhost:5444/clinica`.

2. **Arrancar la API** en `http://localhost:8080` (Flyway crea el esquema y carga la semilla al iniciar):

   ```bash
   .\mvnw.cmd spring-boot:run
   ```

   En Linux/macOS: `./mvnw spring-boot:run`. Si tienes Maven instalado, `mvn spring-boot:run` también funciona.

3. **Abrir Swagger UI**: <http://localhost:8080/swagger-ui.html>
   (especificación OpenAPI en `/v3/api-docs`, salud en `/actuator/health`).

4. **Autenticarse**: obtener un token y usarlo con el botón **Authorize** de Swagger o en la cabecera
   `Authorization: Bearer <token>`.

   ```bash
   curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"mquispe\",\"clave\":\"clinica2026\"}"
   ```

   ```bash
   curl -s http://localhost:8080/api/v1/pacientes/pac-0231/ciclos?especialidad=TO -H "Authorization: Bearer <token>"
   ```

### Usuarios de prueba (clave para todos: `clinica2026`)

| Usuario | Rol | Profesional | Especialidad |
| --- | --- | --- | --- |
| `mquispe` | TERAPEUTA | Lic. María Fernanda Quispe Rojas (`prof-017`) | TO – Terapia Ocupacional |
| `cmendoza` | TERAPEUTA | Lic. Carla Mendoza Paredes (`prof-021`) | TL – Terapia de Lenguaje |
| `jsalazar` | TERAPEUTA | Lic. Jorge Luis Salazar Peña (`prof-019`) | TF – Terapia Física |
| `acastillo` | PSICOLOGO | Ps. Andrea Castillo Ruiz (`prof-033`) | PS – Psicología |
| `coordinador` | COORDINADOR | — | Edita cualquier especialidad |
| `sistemas` | SISTEMAS | — | Solo lectura |

Las credenciales son de desarrollo; en producción se gestionan desde la tabla `usuario` (BCrypt).

### Todo en contenedores (opcional)

```bash
docker compose --profile full up -d --build
```

Construye la imagen de la API (`Dockerfile` multi-stage con JDK 21) y la levanta junto a la base de datos.

## Pruebas

```bash
.\mvnw.cmd test
```

- `ProgresoServiceTest`: porte 1:1 de `clinica-frontend/src/utils/progress.js` (tasa de éxito, acumulado,
  variación, bandas GAS, cierre de ciclo), con los mismos datos del prototipo.
- `CicloServiceTest`: reglas de negocio con repositorios simulados (máx. 3 objetivos → 409, ciclo cerrado,
  especialidad distinta → 403, aciertos ≤ intentos, cierre automático en la sesión 12 con GAS).
- `ClinicaApiIntegrationTest`: extremo a extremo contra **PostgreSQL 16 real** (Testcontainers): migraciones,
  JWT y todo el contrato con los datos semilla. **Se omite automáticamente si Docker no está disponible.**

## Contrato API (`/api/v1`)

Todas las rutas, salvo `POST /auth/login`, Swagger y `/actuator/health`, requieren `Authorization: Bearer <token>`.

| Método | Ruta | Descripción | Respuesta (forma de mockData.js) |
| --- | --- | --- | --- |
| POST | `/auth/login` | Inicia sesión `{usuario, clave}` | `{token, tipo, expiraEnSegundos, usuario}` |
| GET | `/profesionales/me` | Profesional logueado | `currentProfessional` |
| GET | `/pacientes/{id}` | Datos básicos del paciente | `patient` |
| GET | `/pacientes/{id}/ciclos?especialidad=TO` | Ciclo activo de la especialidad | `cycle` + `objectives` (= `activeObjectives`) |
| GET | `/banco-objetivos?especialidad=TO` | Banco de objetivos | `objectiveBank[TO]` |
| POST | `/banco-objetivos` | Crear objetivo en el banco `{text, specialtyId?}` | 201 (o 200 si ya existía) |
| POST | `/ciclos/{id}/objetivos` | Activar objetivo `{bankId, baseline?, target?}` | 201 · **409 si ya hay 3** |
| PATCH | `/ciclos/{id}/objetivos/{objetivoId}` | Ajustar `{baseline?, target?}` | objetivo actualizado |
| DELETE | `/ciclos/{id}/objetivos/{objetivoId}` | Retirar objetivo del ciclo | 204 |
| GET | `/ciclos/{id}/sesiones` | Historial de sesiones (solo lectura) | `sessionHistory` |
| POST | `/ciclos/{id}/sesiones` | Guardar la sesión en curso | 201 · la sesión queda **inmutable** |
| GET | `/ciclos/{id}/progreso` | % acumulado, última sesión, variación y GAS por objetivo | ver abajo |
| GET | `/pacientes/{id}/vista-360` | Progreso en las demás especialidades | `otherSpecialtiesProgress` |
| GET | `/ciclos/{id}/reporte.pdf` | Reporte PDF de la ficha de evolución | `application/pdf` |

Endpoints adicionales (no estaban en el contrato inicial, pero el prototipo o el modelo los necesitan):

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/especialidades` | Catálogo `SPECIALTIES` (`id, name, short, college, color`). |
| GET | `/ciclos/{id}` · `/ciclos/{id}/sesiones/{numero}` | Consultas puntuales. |
| POST | `/pacientes/{id}/ciclos` | Abrir un nuevo ciclo (409 si ya hay uno abierto en esa especialidad). |
| POST | `/ciclos/{id}/cierre` | Cerrar el ciclo y persistir el resultado GAS (`resultado_gas`). |
| GET / POST | `/pacientes/{id}/notas` | Notas interdisciplinarias (`sharedNotes`). |

### Cuerpo de `POST /ciclos/{id}/sesiones`

```json
{
  "number": 4,
  "date": "2026-10-15",
  "evaluations": { "obj-1": { "hits": 6, "total": 10 }, "obj-2": { "hits": 4, "total": 6 } },
  "activity": "Trazos con lápiz adaptado…",
  "achievement": "Pinza trípode en 6 de 10 intentos.",
  "observations": "Continuar con el adaptador en casa."
}
```

`number` es opcional (debe coincidir con la sesión en curso), `date` por defecto es hoy y las claves de
`evaluations` deben ser objetivos activos del ciclo. Al guardar la sesión 12 el ciclo se cierra solo.

### Respuesta de `GET /ciclos/{id}/progreso`

```json
{
  "cycleId": "cic-2026-TO-0231-03", "specialtyId": "TO", "currentSession": 4, "totalSessions": 12,
  "status": "open", "closing": false,
  "objectives": [{
    "id": "obj-1", "bankId": "to-001", "text": "Agarrar el lápiz con pinza trípode",
    "baseline": 10, "target": 80, "accumulated": 33, "lastScore": 50, "lastDelta": 20, "recordedCount": 3,
    "series": [{ "session": 1, "date": "2026-09-24", "value": 20 }, { "session": 2, "date": "2026-10-01", "value": 30 }, { "session": 3, "date": "2026-10-08", "value": 50 }],
    "gas": { "level": -1, "label": "Menor a lo esperado", "decision": "Mantener con ajustes", "tone": "warning" },
    "gasFinal": null
  }]
}
```

Los nombres siguen a las funciones de `progress.js`: `accumulatedProgress`, `lastScore`, `lastDelta`,
`recordedCount`, `scoreSeries`, `computeGas`. `gasFinal` se llena al cerrar el ciclo.

### Errores

Formato *Problem Details* (RFC 9457) con `title` y `detail` en español:
400 solicitud inválida (con `errores` por campo), 401 no autenticado, 403 acceso denegado,
404 recurso no encontrado, 405 método no permitido (p. ej. modificar una sesión), 409 regla de negocio.

## Reglas de dominio

Viven en `domain/progreso/ProgresoService` (porte de `progress.js`) y en `service/CicloService`:

- **% de sesión** = aciertos / intentos, acotado a 100 y redondeado (3 de 10 = 30 %).
- **% acumulado** del objetivo = promedio de las sesiones en que fue evaluado.
- **Escala GAS** con línea base y meta: `≥ meta+15 → +2`, `≥ meta+5 → +1`, `≥ meta−10 → 0`,
  `> línea base → −1`, resto `−2`. El GAS en vivo usa el último % registrado; el definitivo se persiste al cerrar.
- **Máximo 3 objetivos activos por ciclo**: 409 al exceder. Reforzado en la base de datos con un índice único
  parcial sobre `(ciclo_id, orden)` y `orden ∈ {1,2,3}`.
- **Ciclo de 12 sesiones**: `sesion_actual` = sesiones registradas + 1; la sesión 12 cierra el ciclo.
  Un solo ciclo abierto por paciente y especialidad (índice único parcial).
- **Sesiones inmutables**: no existen PUT/PATCH/DELETE (405); la entidad es `@Immutable` y un trigger de
  PostgreSQL rechaza `UPDATE`/`DELETE` sobre `sesion` y `evaluacion_objetivo`.
- **Permisos**: terapeutas y psicólogos leen todo y modifican solo su especialidad; coordinación modifica
  todo; sistemas solo lee.

## Modelo de datos

`especialidad`, `usuario`, `profesional`, `paciente` (diagnósticos en `text[]`), `ciclo`, `banco_objetivo`,
`objetivo_ciclo`, `sesion`, `evaluacion_objetivo`, `resultado_gas`, `nota_compartida`.
Los identificadores de negocio son legibles y coinciden con los del prototipo (`pac-0231`,
`cic-2026-TO-0231-03`, `obj-1`, `to-001`); los nuevos se generan con secuencias (`obj-101`, `to-101`, `note-101`).

## Configuración

| Variable | Por defecto | Descripción |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5433/clinica` | URL JDBC (5433 = puerto publicado por `docker-compose.yml`). |
| `CLINICA_DB_PORT` | `5433` | Puerto del host que publica el contenedor `db` (variable de Docker Compose). |
| `DB_USER` / `DB_PASSWORD` | `clinica` / `clinica` | Credenciales de PostgreSQL. |
| `SERVER_PORT` | `8080` | Puerto HTTP. |
| `APP_JWT_SECRET` | (secreto de desarrollo) | Clave HMAC de al menos 32 bytes. **Cambiar en producción.** |
| `APP_JWT_EXPIRATION_MINUTES` | `480` | Vigencia del token. |
| `APP_CORS_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Orígenes permitidos (front en Vite). |

## Estructura

```
src/main/java/pe/sanjuandedios/clinica
├── config/          SecurityConfig (JWT, CORS, Problem Details), OpenApiConfig, propiedades
├── security/        JwtService, JwtAuthenticationFilter, UsuarioDetailsService, UsuarioAutenticado
├── domain/          Entidades JPA · domain/progreso: ProgresoService, NivelGas (reglas puras)
├── repository/      Spring Data JPA
├── dto/             Records con la forma de mockData.js + Requests
├── mapper/          MapStruct (entidad → DTO)
├── service/         CicloService, BancoObjetivoService, Vista360Service, NotaService, ReportePdfService…
└── web/             Controladores REST · web/error: ApiExceptionHandler y excepciones
src/main/resources/db/migration/   V1__esquema_inicial.sql · V2__datos_semilla.sql
src/test/java/...                  ProgresoServiceTest · CicloServiceTest · ClinicaApiIntegrationTest
```

## Integración con el front

- Reemplazar `useClinicalRecord` por llamadas a los endpoints de la tabla; las respuestas ya tienen los
  nombres de campo que usan los componentes (`fullName`, `historyNumber`, `evaluations[objId].hits`, `history`…).
- `GET /pacientes/{id}/ciclos?especialidad=TO` devuelve el ciclo con `objectives`:
  `const { objectives, ...cycle } = data`.
- En la vista 360°, `currentSession` es el número de la última sesión registrada (como en el prototipo), mientras
  que en el ciclo propio es la sesión en curso.
- CORS permite `http://localhost:5173` por defecto.
