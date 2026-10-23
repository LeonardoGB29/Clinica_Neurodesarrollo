# Sistema de Gestión Clínica Interdisciplinaria

**Centro de Neurodesarrollo (TEA/TDAH) · Clínica San Juan de Dios**

Ficha de evolución digital para las cuatro terapias del centro (Ocupacional, Lenguaje, Física y Psicología).
Cada profesional trabaja un **ciclo de 12 sesiones** con **máximo 3 objetivos**, registra en cada sesión la tasa
de éxito de cada objetivo (aciertos / intentos → %), sigue el progreso acumulado en un dashboard, obtiene la
**escala GAS** (-2 … +2) al cierre del ciclo para decidir si sube, mantiene o baja la carga, y consulta en una
**vista 360°** lo que avanzan las demás especialidades con el mismo paciente.

## Estructura

| Carpeta | Qué contiene | Tecnología |
| --- | --- | --- |
| [`clinica-frontend/`](clinica-frontend/README.md) | Interfaz web: login, cabecera de sesión, gestión de objetivos, dashboard, historial en cascada, vista 360°, reporte PDF | React 19 · Vite 8 · Tailwind CSS 4 · Recharts |
| [`clinica-backend/`](clinica-backend/README.md) | API REST, reglas de negocio, seguridad JWT por rol, generación del PDF, migraciones y datos de demostración | Java 21 · Spring Boot 3.5 · PostgreSQL 16 · Flyway |
| [`STACK_TECNOLOGICO.md`](STACK_TECNOLOGICO.md) | Stack completo, modelo de datos y contrato de la API | — |

## Requisitos

- Docker Desktop (para PostgreSQL)
- JDK 21 o superior (Maven no hace falta: el proyecto incluye el wrapper `mvnw`)
- Node.js 22 LTS (mínimo 20.19) y npm

## Levantar el proyecto

Tres pasos, en este orden.

**1. Base de datos** — en `clinica-backend/` (PostgreSQL 16 en el puerto 5433):

```bash
docker compose up -d db
```

**2. API** — en `clinica-backend/`. Queda en <http://localhost:8080> (Swagger: <http://localhost:8080/swagger-ui.html>).
La primera vez Flyway crea el esquema y carga los datos de demostración:

```bash
.\mvnw.cmd spring-boot:run
```

En Linux/macOS: `./mvnw spring-boot:run`.

**3. Frontend** — en `clinica-frontend/`. Queda en <http://localhost:5173>:

```bash
npm install
```

```bash
npm run dev
```

Ingresar con **`mquispe` / `clinica2026`** (Terapia Ocupacional): se abre la ficha del paciente de demostración
en la sesión 4 de 12. Con la misma clave: `cmendoza` (Lenguaje), `jsalazar` (Física), `acastillo` (Psicología).

## Qué probar

1. Agregar un objetivo del banco o crear uno nuevo con **+**; al tercero, el selector se bloquea.
2. Registrar aciertos/intentos, actividad y logro de la sesión 4 y **Guardar sesión**: pasa a solo lectura y se abre la 5.
3. Revisar el dashboard (acumulado, última sesión, gráfico) y activar **Simular cierre de ciclo** para ver la escala GAS.
4. Abrir la **Vista 360°** (botón arriba a la derecha o entrada del menú lateral) para ver el avance de las demás especialidades y las notas compartidas; se cierra con Esc.
5. **Generar Reporte PDF**.

## Pruebas del backend

En `clinica-backend/`:

```bash
.\mvnw.cmd test
```

Las pruebas de integración levantan PostgreSQL con Testcontainers y se omiten si Docker no está disponible.

## Reiniciar los datos de demostración

Las sesiones guardadas son inmutables. Para volver al estado inicial, detener la API y, en `clinica-backend/`:

```bash
docker compose down -v
```

```bash
docker compose up -d db
```

Luego arrancar la API otra vez (Flyway vuelve a cargar la semilla).

## Limitación conocida

Al abrir un ciclo nuevo (`POST /pacientes/{id}/ciclos`) el backend numera el ciclo contando los existentes, y los
datos de demostración ya usan los sufijos 02 y 03: en Lenguaje y Psicología el primer ciclo nuevo puede chocar con
el ciclo cerrado y sobrescribirlo. Pendiente de corregir en `CicloService.crear` (usar el mayor sufijo + 1 y
comprobar que el id no exista antes de guardar).
