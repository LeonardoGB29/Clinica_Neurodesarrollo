# Frontend · Ficha de Evolución y Dashboard

Sistema de Gestión Clínica Interdisciplinaria — Centro de Neurodesarrollo (TEA/TDAH), Clínica San Juan de Dios.

Aplicación **React 19 + Vite 8 + Tailwind CSS 4** conectada a la API REST de [`../clinica-backend`](../clinica-backend/README.md)
(Spring Boot 3.5 + PostgreSQL 16). Nació como maqueta con datos mock para validar flujos con terapeutas, psicólogos
y el área de Sistemas; hoy toda la información (profesional, paciente, ciclo, objetivos, sesiones, vista 360°) se
lee y se guarda en el backend, con inicio de sesión por JWT.

## Requisitos

- Node.js 22 LTS (mínimo 20.19) y npm 10
- El backend en ejecución (ver [Conexión con el backend](#conexión-con-el-backend))

## Ejecutar

```bash
npm install
npm run dev
```

Abrir <http://localhost:5173>, iniciar sesión con un usuario de prueba (abajo) y se carga la ficha del paciente
`pac-0231` con los datos semilla del backend (idénticos al antiguo mock). Para generar la versión de producción:
`npm run build` (salida en `dist/`).

## Conexión con el backend

### 1. Levantar la API

En `../clinica-backend/` (requiere Docker Desktop y JDK 21+; Maven no hace falta, se usa el *wrapper*):

```bash
docker compose up -d db
```

```bash
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080` (Swagger en <http://localhost:8080/swagger-ui.html>) y ya permite CORS
desde `http://localhost:5173`. Flyway crea el esquema y carga los datos semilla la primera vez.

### 2. Configurar la URL de la API (`VITE_API_URL`)

Por defecto el front usa `http://localhost:8080/api/v1`. Para apuntar a otra dirección, copia `.env.example` como
`.env.local` (ignorado por git) y ajusta:

```bash
VITE_API_URL=http://localhost:8080/api/v1
VITE_PACIENTE_ID=pac-0231
```

| Variable | Por defecto | Descripción |
| --- | --- | --- |
| `VITE_API_URL` | `http://localhost:8080/api/v1` | URL base de la API. Detrás de Nginx puede ser relativa (`/api/v1`). |
| `VITE_PACIENTE_ID` | `pac-0231` | Paciente que abre la ficha hasta que exista el módulo Pacientes. |

Las variables `VITE_*` se leen al arrancar Vite: tras cambiarlas, reinicia `npm run dev` (o vuelve a ejecutar
`npm run build`).

### 3. Credenciales de prueba (clave para todos: `clinica2026`)

| Usuario | Rol | Profesional | Especialidad |
| --- | --- | --- | --- |
| `mquispe` | TERAPEUTA | Lic. María Fernanda Quispe Rojas | TO – Terapia Ocupacional (la ficha del prototipo) |
| `cmendoza` | TERAPEUTA | Lic. Carla Mendoza Paredes | TL – Terapia de Lenguaje |
| `jsalazar` | TERAPEUTA | Lic. Jorge Luis Salazar Peña | TF – Terapia Física |
| `acastillo` | PSICOLOGO | Ps. Andrea Castillo Ruiz | PS – Psicología |

Cada usuario ve el ciclo abierto de **su** especialidad para el paciente y, en la vista 360°, las demás. Los usuarios
`coordinador` y `sistemas` existen en el backend pero no tienen profesional ni especialidad asociados, por lo que la
ficha de evolución muestra un aviso y no se abre con ellos. En modo desarrollo la pantalla de login ofrece botones
para rellenar estas credenciales.

### Cómo funciona la sesión

- `POST /auth/login {usuario, clave}` devuelve el JWT y los datos del usuario (`profesionalId`, `specialtyId`). Se
  guardan en `sessionStorage` (`src/api/session.js`): sobreviven a recargas y se pierden al cerrar la pestaña.
- Todas las peticiones salen con `Authorization: Bearer <token>` (`src/api/client.js`). Si la API responde **401**
  (token vencido o inválido) la sesión se descarta y se vuelve al login con un aviso.
- Los errores llegan en formato *Problem Details* (`title`/`detail` en español) y se muestran con el Toast existente;
  por ejemplo, al intentar activar un cuarto objetivo la API responde **409** «Límite alcanzado: máximo 3 objetivos
  activos por ciclo.».
- **Generar Reporte PDF** descarga `GET /ciclos/{id}/reporte.pdf` con el token y lo abre en una pestaña nueva.

### Abrir un nuevo ciclo

Cuando el paciente no tiene un ciclo abierto de la especialidad del profesional, `GET /pacientes/{id}/ciclos?especialidad=`
responde **404** (por ejemplo, tras guardar la sesión 12, que cierra el ciclo). `useClinicalRecord` distingue ese 404 de
los demás errores y pasa a `status: "no-cycle"` con el paciente y el profesional ya cargados; `App` muestra entonces la
tarjeta **Sin ciclo abierto de \<especialidad\>** (`components/cycle/NoCycleCard.jsx`) con la fecha de inicio (hoy por
defecto) y la frecuencia. El botón **Abrir nuevo ciclo de 12 sesiones** llama a `POST /pacientes/{id}/ciclos
{specialtyId, startDate, frequency, totalSessions}` (201) y la ficha carga con la *Sesión 1 de 12* sin objetivos; si la
API responde **409** (ya existe un ciclo abierto, p. ej. creado desde otra pestaña) o **403** se avisa con el Toast y, en
el 409, se recarga la ficha para mostrar el ciclo existente. El mismo formulario (`NewCycleForm.jsx`) aparece en el bloque
*Ciclo completado* del historial justo después de cerrar el ciclo, y el botón **Abrir siguiente ciclo** de la cabecera
lleva hasta él.

### Volver a los datos semilla

Las sesiones guardadas son inmutables en el backend. Para repetir la demostración desde la sesión 4, en
`../clinica-backend/` recrea la base de datos (borra el volumen) y reinicia la API para que Flyway vuelva a cargar
la semilla:

```bash
docker compose down -v
```

```bash
docker compose up -d db
```

y vuelve a ejecutar `.\mvnw.cmd spring-boot:run`.

## Qué incluye la vista principal

| Módulo | Componente | Qué valida |
| --- | --- | --- |
| 0. Inicio de sesión | `components/auth/LoginScreen.jsx` | Usuario y contraseña contra `POST /auth/login`; errores de credenciales y de conexión. |
| 1. Cabecera y datos de sesión | `components/header/SessionHeader.jsx` | Profesional (nombre, especialidad, CTMP), paciente (HC, edad, diagnósticos, apoderado), indicador *Sesión 4 de 12* con stepper de 12 semanas, botones **Generar Reporte PDF** y **Guardar sesión**. |
| 2. Gestión de objetivos | `components/objectives/ObjectivesPanel.jsx` | Dropdown del banco de la especialidad logueada, creación de objetivo nuevo con **+** (queda guardado en el banco), tarjetas de objetivos activos con línea base y meta editables, límite de 3 por ciclo (validado por la API). |
| 3. Dashboard de progreso | `components/dashboard/ProgressDashboard.jsx`, `GasScale.jsx`, `CycleChart.jsx` | Anillo de % acumulado por objetivo, última sesión con variación, mini-columnas S1…S12, escala GAS (-2…+2) bloqueada hasta el cierre del ciclo con proyección, gráfico de evolución por sesión (Recharts). Incluye un interruptor para **simular el cierre de ciclo**. |
| 4. Historial de evolución (cascada) | `components/evolution/EvolutionTimeline.jsx`, `SessionRecord.jsx`, `EvolutionForm.jsx` | Sesiones registradas en solo lectura seguidas del formulario editable de la sesión en curso: aciertos/intentos por objetivo (→ %), Actividad realizada / Evolución, Logro, Observaciones y recomendaciones. Al guardar, la sesión pasa a solo lectura y se abre la siguiente. |
| 5. Vista 360° | `components/interdisciplinary/View360.jsx` | Panel lateral de solo lectura con objetivos, % y tendencia del paciente en las demás especialidades, última observación de cada área y notas compartidas. |
| 6. Nuevo ciclo | `components/cycle/NoCycleCard.jsx`, `NewCycleForm.jsx` | Tarjeta **Sin ciclo abierto** cuando la API responde 404 para la especialidad, y formulario (fecha de inicio, frecuencia) para abrir el siguiente ciclo de 12 sesiones; el mismo formulario aparece al completar el ciclo actual. |

## Estructura

```
src/
├── App.jsx                     # Sesión (login / ficha) y composición de la vista principal
├── index.css                   # Tailwind 4 + tokens de marca (@theme) + estilos de impresión
├── api/
│   ├── client.js               # fetch con base URL (VITE_API_URL), JWT, Problem Details → ApiError, 401 → login
│   ├── clinicaApi.js           # Un método por endpoint del contrato (/auth, /pacientes, /ciclos, /banco-objetivos…)
│   └── session.js              # Token y usuario en sessionStorage
├── hooks/
│   ├── useAuth.js              # login / logout / sesión vigente
│   └── useClinicalRecord.js    # Estado y acciones de la ficha contra la API (único punto de integración)
├── data/mockData.js            # Referencia de la forma de los DTOs (ya no lo usa la app; coincide con la semilla)
├── utils/progress.js           # Reglas de cálculo: % de sesión, acumulado, GAS (también portadas al backend)
├── utils/theme.js              # Colores de serie (validados para daltonismo) y tonos
├── assets/                     # Logo de la clínica (SVG/PNG)
└── components/
    ├── auth/      LoginScreen
    ├── cycle/     NoCycleCard, NewCycleForm
    ├── ui/        Card, Badge, Button, ProgressBar, RingProgress, Sparkline, Avatar, Toast, StatusScreen
    ├── layout/    Sidebar, Topbar
    ├── header/    SessionHeader
    ├── objectives/ObjectivesPanel
    ├── dashboard/ ProgressDashboard, GasScale, CycleChart
    ├── evolution/ EvolutionTimeline, SessionRecord, EvolutionForm
    └── interdisciplinary/View360
```

### Endpoints que consume `useClinicalRecord`

| Momento | Llamadas |
| --- | --- |
| Carga inicial | `GET /profesionales/me`, `GET /especialidades` (lista → mapa por id), `GET /pacientes/{id}`, `GET /pacientes/{id}/ciclos?especialidad=` (`{...cycle, objectives}` se separa en `cycle` y `objectives`; si responde 404 la ficha pasa al estado *sin ciclo*), `GET /ciclos/{id}/sesiones`, `GET /banco-objetivos?especialidad=`, `GET /pacientes/{id}/vista-360`, `GET /pacientes/{id}/notas` |
| Abrir ciclo | `POST /pacientes/{id}/ciclos {specialtyId, startDate, frequency, totalSessions}` (201 · 409 si ya hay uno abierto) y `GET /ciclos/{id}/sesiones` del ciclo nuevo |
| Agregar del banco | `POST /ciclos/{id}/objetivos {bankId}` y relectura de `GET /ciclos/{id}` |
| Crear objetivo | `POST /banco-objetivos {text}` y, si hay cupo, `POST /ciclos/{id}/objetivos` |
| Línea base / meta | `PATCH /ciclos/{id}/objetivos/{objetivoId} {baseline, target}` (con un pequeño retardo mientras se escribe; si la API lo rechaza se restaura el valor guardado) |
| Retirar objetivo | `DELETE /ciclos/{id}/objetivos/{objetivoId}` |
| Guardar sesión | `POST /ciclos/{id}/sesiones {number, date, evaluations, activity, achievement, observations}` y relectura de ciclo + sesiones (en la sesión 12 el backend cierra el ciclo) |
| Reporte PDF | `GET /ciclos/{id}/reporte.pdf` (blob) |

El borrador de la sesión en curso (`draft` / `draftErrors`) sigue viviendo solo en el navegador hasta que se guarda.

## Reglas de cálculo (ajustables con el equipo clínico)

El dashboard calcula en el navegador con `src/utils/progress.js`; el backend tiene el mismo porte en
`domain/progreso/ProgresoService` y lo expone en `GET /ciclos/{id}/progreso`.

- **% de la sesión** = aciertos / intentos (ej. 3 de 10 = 30 %).
- **% acumulado del objetivo** = promedio de los % de las sesiones registradas. Si el equipo prefiere "último valor" o "avance entre línea base y meta", solo cambia `accumulatedProgress`.
- **Escala GAS** (se habilita en la sesión 12 o con el interruptor de simulación) a partir del último % registrado, la línea base y la meta:
  - `≥ meta + 15` → **+2** (subir carga / nuevo objetivo)
  - `≥ meta + 5` → **+1** (subir carga)
  - `≥ meta − 10` → **0** (mantener)
  - `> línea base` → **−1** (mantener con ajustes)
  - `≤ línea base` → **−2** (bajar carga)

## Flujos sugeridos para la validación

1. Iniciar sesión como `mquispe` y comprobar que la cabecera, los objetivos y las 3 sesiones coinciden con la semilla.
2. Agregar un tercer objetivo desde el banco y comprobar que el selector se bloquea (límite de 3).
3. Crear un objetivo nuevo con el botón **+** y verificar que aparece en el banco (persiste al recargar).
4. Registrar aciertos/intentos en la sesión 4, completar Actividad y Logro y pulsar **Guardar sesión 4**: la sesión pasa a solo lectura, el header cambia a *Sesión 5 de 12* y el dashboard se actualiza con lo que devuelve la API.
5. Intentar guardar con campos vacíos para ver las validaciones (y las de la API, p. ej. aciertos > intentos).
6. Activar **Simular cierre de ciclo** para revisar la escala GAS y la decisión de carga.
7. Pulsar **Generar Reporte PDF**: se abre en una pestaña nueva el PDF generado por el backend.
8. Cerrar sesión (icono junto al nombre en la barra lateral) e ingresar como `cmendoza` para ver la ficha de Lenguaje.
9. Cerrar un ciclo (guardando hasta la sesión 12, o con `POST /ciclos/{id}/cierre` desde Swagger) y abrir el siguiente
   desde el bloque *Ciclo completado* del historial o, tras recargar, desde la tarjeta *Sin ciclo abierto*: la ficha
   vuelve a *Sesión 1 de 12* sin objetivos.

## Siguiente hito

- TypeScript + TanStack Query para tipar los DTOs y cachear/revalidar las consultas.
- React Router: módulos Agenda, Pacientes (selección del paciente en lugar de `VITE_PACIENTE_ID`), Reportes y Configuración.
- Consumir `GET /ciclos/{id}/progreso` en lugar de recalcular en el navegador.
- Historial de ciclos anteriores del paciente (hoy la ficha solo muestra el ciclo abierto o el recién cerrado).
