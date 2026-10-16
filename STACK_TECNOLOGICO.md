# Stack tecnológico y herramientas

**Sistema de Gestión Clínica Interdisciplinaria — Centro de Neurodesarrollo (TEA/TDAH)**
Clínica San Juan de Dios · Documento de referencia para el equipo de desarrollo y el área de Sistemas.

---

## 1. Arquitectura general

```
┌────────────────────────────┐      HTTPS / JSON       ┌──────────────────────────────┐
│  Frontend (SPA)            │ ──────────────────────▶ │  Backend (API REST)          │
│  React 19 + Vite 8         │ ◀────────────────────── │  Spring Boot 3.5 · Java 21   │
│  Tailwind CSS 4 · Recharts │   JWT en cada petición  │  Spring Security · JPA       │
└────────────────────────────┘                         └──────────────┬───────────────┘
                                                                      │ JDBC
                                                       ┌──────────────▼───────────────┐
                                                       │  PostgreSQL 16               │
                                                       │  Migraciones con Flyway      │
                                                       └──────────────────────────────┘
```

- **Frontend**: aplicación de una sola página (SPA) servida por Nginx; consume la API y no contiene lógica clínica.
- **Backend**: expone la API, aplica las reglas de negocio (máx. 3 objetivos, ciclo de 12 sesiones, cálculo de % y escala GAS), genera los PDF y controla los permisos por rol.
- **Base de datos**: PostgreSQL con esquema versionado.

---

## 2. Frontend

### 2.1 Prototipo actual (hito de validación con datos mock)

| Herramienta | Versión | Para qué se usa |
| --- | --- | --- |
| **React** | 19 | Interfaz por componentes funcionales + hooks (`useState`, `useMemo`, `useCallback`). |
| **Vite** | 8 | Servidor de desarrollo con recarga instantánea y empaquetado para producción. |
| **Tailwind CSS** | 4 | Estilos utilitarios. Paleta corporativa definida como tokens (`@theme`): `#F4F6F8`, `#0083A8`, `#0B3C5D`, `#FF9E1B`. |
| **Recharts** | 3 | Gráfico de evolución por sesión del dashboard. |
| **lucide-react** | 1.x | Iconografía consistente (SVG). |
| **Node.js / npm** | 22 LTS / 10 | Entorno de herramientas y gestor de paquetes. |

### 2.2 Versión conectada al backend (siguiente hito)

| Herramienta | Para qué se usa |
| --- | --- |
| **TypeScript 5** | Tipar los DTOs de la API y evitar errores de integración. |
| **React Router 7** | Navegación entre Agenda, Pacientes, Ficha de evolución, Reportes y Configuración. |
| **TanStack Query 5** | Consumo de la API con caché, estados de carga/error y revalidación. |
| **Axios** | Cliente HTTP con interceptores para adjuntar el JWT y manejar expiración de sesión. |
| **React Hook Form + Zod** | Formulario de evolución con validación declarativa (aciertos ≤ intentos, campos obligatorios). |
| **Zustand** | Estado global ligero (usuario logueado, paciente seleccionado). |
| **ESLint + Prettier** | Calidad y formato de código uniforme. |
| **Vitest + React Testing Library** | Pruebas unitarias de reglas de cálculo y de componentes. |
| **Playwright** | Pruebas end-to-end de los flujos clínicos (registrar sesión, cerrar ciclo). |

### 2.3 Diseño

| Herramienta | Para qué se usa |
| --- | --- |
| **Figma** | Wireframes, prototipo navegable y biblioteca de componentes con la identidad visual de la clínica. |
| **Validador de paleta** (script del proyecto) | Comprobación de contraste y legibilidad para daltonismo de los colores de gráficos. |

---

## 3. Backend

| Herramienta | Versión | Para qué se usa |
| --- | --- | --- |
| **Java** | 21 LTS | Lenguaje de la API (records, pattern matching, virtual threads). |
| **Spring Boot** | 3.5 | Base del backend: configuración automática, empaquetado ejecutable. |
| **Spring Web (MVC)** | 6.2 | Controladores REST y manejo global de errores (`@RestControllerAdvice`). |
| **Spring Data JPA + Hibernate** | 3.5 / 6.6 | Persistencia de pacientes, ciclos, objetivos y sesiones. |
| **Spring Security + JWT** | 6.5 · `jjwt` | Autenticación y autorización por rol: terapeuta, psicólogo, coordinador, sistemas. |
| **Jakarta Bean Validation** | 3.1 | Validaciones de DTOs (máx. 3 objetivos por ciclo, 12 sesiones, aciertos ≤ intentos). |
| **Flyway** | 11 | Migraciones versionadas del esquema PostgreSQL. |
| **MapStruct + Lombok** | 1.6 / 1.18 | Mapeo entidad ↔ DTO y reducción de código repetitivo. |
| **springdoc-openapi** | 2.8 | Documentación Swagger UI del contrato de la API. |
| **OpenPDF** (o **JasperReports**) | 2.x / 7.x | Generación del *Reporte PDF* de la ficha de evolución. |
| **Spring Boot Actuator** | 3.5 | Endpoints de salud y métricas para operación. |
| **Spring Mail** (opcional) | 3.5 | Envío de reportes o recordatorios de sesión. |
| **JUnit 5 + Mockito + Spring Boot Test** | 5.11 / 5.x | Pruebas unitarias y de integración. |
| **Testcontainers** | 1.20 | Pruebas de integración contra un PostgreSQL real en Docker. |
| **Maven** | 3.9 | Construcción y gestión de dependencias (alternativa: Gradle 8). |

---

## 4. Base de datos

| Herramienta | Para qué se usa |
| --- | --- |
| **PostgreSQL 16** | Base de datos relacional principal. |
| **pgAdmin 4 / DBeaver** | Administración y consultas. |
| **Flyway** | Scripts `V1__...sql` versionados en el repositorio del backend. |

### Modelo de datos (resumen)

| Tabla | Campos clave | Notas |
| --- | --- | --- |
| `especialidad` | id, nombre, colegio_profesional | TO, TL, TF, PS. |
| `profesional` | id, nombres, especialidad_id, colegio, nro_colegiatura, usuario_id | Datos precargados en la cabecera. |
| `paciente` | id, nro_historia, nombres, fecha_nac, sexo, diagnosticos, apoderado, cobertura | |
| `ciclo` | id, paciente_id, especialidad_id, fecha_inicio, total_sesiones = 12, sesion_actual, estado | Un ciclo activo por paciente y especialidad. |
| `banco_objetivo` | id, especialidad_id, texto, creado_por | Reutilizable en futuros ciclos. |
| `objetivo_ciclo` | id, ciclo_id, banco_objetivo_id, linea_base, meta, sesion_inicio, orden | Restricción: máx. 3 por ciclo. |
| `sesion` | id, ciclo_id, numero, fecha, profesional_id, actividad, logro, observaciones | Inmutable una vez guardada (solo lectura). |
| `evaluacion_objetivo` | id, sesion_id, objetivo_ciclo_id, aciertos, intentos | % = aciertos / intentos. |
| `resultado_gas` | id, objetivo_ciclo_id, nivel (-2…+2), decision, fecha | Se genera al cerrar el ciclo. |

---

## 5. Infraestructura, DevOps y herramientas de equipo

| Herramienta | Para qué se usa |
| --- | --- |
| **Git + GitHub** | Control de versiones, pull requests y revisión de código. |
| **Docker + Docker Compose** | Entorno reproducible: `api` (Spring Boot), `db` (PostgreSQL), `web` (Nginx con la SPA). |
| **GitHub Actions** | Integración continua: build y pruebas del front y del back en cada PR. |
| **Nginx** | Servir la SPA y actuar como proxy inverso hacia la API. |
| **Postman / Bruno** | Colección de pruebas manuales de la API. |
| **SonarCloud** (opcional) | Análisis estático de calidad y seguridad. |
| **Sentry** (opcional) | Monitoreo de errores en producción (front y back). |
| **Jira / Trello** | Gestión del backlog y seguimiento de hitos. |

---

## 6. Contrato API inicial (lo que el prototipo espera)

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/v1/profesionales/me` | Datos del profesional logueado (nombre, especialidad, colegiatura). |
| GET | `/api/v1/pacientes/{id}` | Datos básicos del paciente. |
| GET | `/api/v1/pacientes/{id}/ciclos?especialidad=TO` | Ciclo activo de la especialidad (sesión actual, total, objetivos). |
| GET | `/api/v1/banco-objetivos?especialidad=TO` | Banco de objetivos de la especialidad. |
| POST | `/api/v1/banco-objetivos` | Crear objetivo nuevo en el banco. |
| POST | `/api/v1/ciclos/{id}/objetivos` | Activar objetivo en el ciclo (409 si ya hay 3). |
| PATCH | `/api/v1/ciclos/{id}/objetivos/{objetivoId}` | Ajustar línea base / meta. |
| DELETE | `/api/v1/ciclos/{id}/objetivos/{objetivoId}` | Retirar objetivo del ciclo. |
| GET | `/api/v1/ciclos/{id}/sesiones` | Historial de sesiones (solo lectura). |
| POST | `/api/v1/ciclos/{id}/sesiones` | Guardar la sesión en curso (evaluaciones + textos). |
| GET | `/api/v1/ciclos/{id}/progreso` | % acumulado, última sesión, variación y GAS por objetivo. |
| GET | `/api/v1/pacientes/{id}/vista-360` | Objetivos y progreso en las demás especialidades. |
| GET | `/api/v1/ciclos/{id}/reporte.pdf` | Reporte PDF de la ficha de evolución. |

Los objetos de `src/data/mockData.js` del prototipo tienen la misma forma que las respuestas esperadas, de modo que la integración se reduce a reemplazar el hook `useClinicalRecord` por llamadas a estos endpoints.
