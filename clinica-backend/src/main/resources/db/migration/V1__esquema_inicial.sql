-- ============================================================================
--  V1 – Esquema inicial
--  Sistema de Gestión Clínica Interdisciplinaria · Centro de Neurodesarrollo (TEA/TDAH)
--  Clínica San Juan de Dios
--
--  Convenciones:
--   · Los identificadores de negocio (especialidad, profesional, paciente, ciclo,
--     banco de objetivos, objetivo del ciclo, nota) son VARCHAR legibles, iguales
--     a los que ya usa el prototipo del front (ej. "pac-0231", "cic-2026-TO-0231-03").
--   · Las tablas de alto volumen (sesion, evaluacion_objetivo, resultado_gas)
--     usan identidad numérica.
--   · Las reglas de negocio críticas se refuerzan también en la base de datos:
--     máx. 3 objetivos activos por ciclo, un ciclo abierto por paciente y
--     especialidad, sesiones inmutables.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Especialidades del centro: TO, TL, TF, PS
-- ---------------------------------------------------------------------------
CREATE TABLE especialidad (
    id                  VARCHAR(4)  PRIMARY KEY,
    nombre              VARCHAR(80) NOT NULL,
    nombre_corto        VARCHAR(40) NOT NULL,
    colegio_profesional VARCHAR(20) NOT NULL,          -- CTMP, CPsP
    color               VARCHAR(7)  NOT NULL,          -- color de la especialidad en la UI (#RRGGBB)
    orden               INTEGER     NOT NULL           -- orden de presentación
);

-- ---------------------------------------------------------------------------
-- Usuarios del sistema (credenciales y rol). Un profesional tiene un usuario.
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
    id             VARCHAR(40)  PRIMARY KEY,
    nombre_usuario VARCHAR(60)  NOT NULL UNIQUE,
    clave_hash     VARCHAR(100) NOT NULL,              -- BCrypt
    rol            VARCHAR(20)  NOT NULL CHECK (rol IN ('TERAPEUTA', 'PSICOLOGO', 'COORDINADOR', 'SISTEMAS')),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Profesionales (datos precargados en la cabecera de la ficha)
-- ---------------------------------------------------------------------------
CREATE TABLE profesional (
    id              VARCHAR(40)  PRIMARY KEY,
    nombres         VARCHAR(120) NOT NULL,
    iniciales       VARCHAR(4)   NOT NULL,
    especialidad_id VARCHAR(4)   NOT NULL REFERENCES especialidad (id),
    colegio         VARCHAR(20)  NOT NULL,
    nro_colegiatura VARCHAR(20)  NOT NULL,
    correo          VARCHAR(120),
    usuario_id      VARCHAR(40)  UNIQUE REFERENCES usuario (id),
    UNIQUE (colegio, nro_colegiatura)
);

-- ---------------------------------------------------------------------------
-- Pacientes
-- ---------------------------------------------------------------------------
CREATE TABLE paciente (
    id                 VARCHAR(40)  PRIMARY KEY,
    nro_historia       VARCHAR(30)  NOT NULL UNIQUE,
    nombres            VARCHAR(120) NOT NULL,
    iniciales          VARCHAR(4)   NOT NULL,
    fecha_nac          DATE         NOT NULL,
    sexo               VARCHAR(1)   NOT NULL CHECK (sexo IN ('M', 'F')),
    diagnosticos       TEXT[]       NOT NULL DEFAULT '{}',   -- ej. "TEA nivel 1 (CIE-11 6A02.0)"
    apoderado_nombre   VARCHAR(120),
    apoderado_relacion VARCHAR(40),
    apoderado_telefono VARCHAR(30),
    cobertura          VARCHAR(120),
    colegio            VARCHAR(120),                         -- institución educativa
    creado_en          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Ciclo terapéutico: 12 sesiones (12 semanas) por paciente y especialidad
-- ---------------------------------------------------------------------------
CREATE TABLE ciclo (
    id              VARCHAR(40) PRIMARY KEY,
    paciente_id     VARCHAR(40) NOT NULL REFERENCES paciente (id),
    especialidad_id VARCHAR(4)  NOT NULL REFERENCES especialidad (id),
    profesional_id  VARCHAR(40) NOT NULL REFERENCES profesional (id),   -- profesional a cargo
    fecha_inicio    DATE        NOT NULL,
    total_sesiones  INTEGER     NOT NULL DEFAULT 12 CHECK (total_sesiones BETWEEN 1 AND 52),
    sesion_actual   INTEGER     NOT NULL DEFAULT 1  CHECK (sesion_actual >= 1),   -- sesión en curso (registradas + 1)
    frecuencia      VARCHAR(60),
    max_objetivos   INTEGER     NOT NULL DEFAULT 3  CHECK (max_objetivos BETWEEN 1 AND 3),
    estado          VARCHAR(10) NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO', 'CERRADO')),
    fecha_cierre    DATE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (sesion_actual <= total_sesiones + 1)
);
-- Un solo ciclo abierto por paciente y especialidad.
CREATE UNIQUE INDEX ux_ciclo_abierto_por_especialidad ON ciclo (paciente_id, especialidad_id) WHERE estado = 'ABIERTO';
CREATE INDEX ix_ciclo_paciente ON ciclo (paciente_id);

-- ---------------------------------------------------------------------------
-- Banco de objetivos por especialidad (reutilizable en futuros ciclos)
-- ---------------------------------------------------------------------------
CREATE TABLE banco_objetivo (
    id              VARCHAR(40)  PRIMARY KEY,
    especialidad_id VARCHAR(4)   NOT NULL REFERENCES especialidad (id),
    texto           VARCHAR(300) NOT NULL,
    creado_por      VARCHAR(40)  REFERENCES profesional (id),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (especialidad_id, texto)
);
CREATE SEQUENCE banco_objetivo_seq START WITH 101;

-- ---------------------------------------------------------------------------
-- Objetivos activados en un ciclo (máximo 3 activos por ciclo)
--   linea_base / meta: % de éxito inicial y esperado al cierre (nivel 0 GAS)
-- ---------------------------------------------------------------------------
CREATE TABLE objetivo_ciclo (
    id                VARCHAR(40) PRIMARY KEY,
    ciclo_id          VARCHAR(40) NOT NULL REFERENCES ciclo (id),
    banco_objetivo_id VARCHAR(40) NOT NULL REFERENCES banco_objetivo (id),
    linea_base        INTEGER     NOT NULL CHECK (linea_base BETWEEN 0 AND 100),
    meta              INTEGER     NOT NULL CHECK (meta BETWEEN 0 AND 100),
    sesion_inicio     INTEGER     NOT NULL CHECK (sesion_inicio >= 1),
    orden             INTEGER     NOT NULL CHECK (orden BETWEEN 1 AND 3),
    activo            BOOLEAN     NOT NULL DEFAULT TRUE,
    retirado_en       TIMESTAMPTZ,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (meta > linea_base)
);
-- Con orden en {1,2,3} y único entre los activos, la base de datos garantiza el máximo de 3.
CREATE UNIQUE INDEX ux_objetivo_ciclo_orden_activo ON objetivo_ciclo (ciclo_id, orden) WHERE activo;
CREATE UNIQUE INDEX ux_objetivo_ciclo_banco_activo ON objetivo_ciclo (ciclo_id, banco_objetivo_id) WHERE activo;
CREATE INDEX ix_objetivo_ciclo_ciclo ON objetivo_ciclo (ciclo_id);
CREATE SEQUENCE objetivo_ciclo_seq START WITH 101;

-- ---------------------------------------------------------------------------
-- Sesiones registradas (inmutables una vez guardadas)
-- ---------------------------------------------------------------------------
CREATE TABLE sesion (
    id             BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ciclo_id       VARCHAR(40) NOT NULL REFERENCES ciclo (id),
    numero         INTEGER     NOT NULL CHECK (numero >= 1),
    fecha          DATE        NOT NULL,
    profesional_id VARCHAR(40) NOT NULL REFERENCES profesional (id),
    actividad      TEXT        NOT NULL,
    logro          TEXT        NOT NULL,
    observaciones  TEXT,
    registrado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (ciclo_id, numero)
);

-- ---------------------------------------------------------------------------
-- Evaluación de cada objetivo en una sesión: % = aciertos / intentos
-- ---------------------------------------------------------------------------
CREATE TABLE evaluacion_objetivo (
    id                BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sesion_id         BIGINT      NOT NULL REFERENCES sesion (id) ON DELETE CASCADE,
    objetivo_ciclo_id VARCHAR(40) NOT NULL REFERENCES objetivo_ciclo (id),
    aciertos          INTEGER     NOT NULL CHECK (aciertos >= 0),
    intentos          INTEGER     NOT NULL CHECK (intentos >= 1),
    CHECK (aciertos <= intentos),
    UNIQUE (sesion_id, objetivo_ciclo_id)
);
CREATE INDEX ix_evaluacion_objetivo ON evaluacion_objetivo (objetivo_ciclo_id);

-- ---------------------------------------------------------------------------
-- Resultado GAS (Goal Attainment Scaling) por objetivo, generado al cerrar el ciclo
-- ---------------------------------------------------------------------------
CREATE TABLE resultado_gas (
    id                BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    objetivo_ciclo_id VARCHAR(40) NOT NULL UNIQUE REFERENCES objetivo_ciclo (id),
    nivel             INTEGER     NOT NULL CHECK (nivel BETWEEN -2 AND 2),
    puntaje           INTEGER     NOT NULL CHECK (puntaje BETWEEN 0 AND 100),   -- % con el que se calculó el nivel
    decision          VARCHAR(60) NOT NULL,
    fecha             DATE        NOT NULL,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Notas interdisciplinarias compartidas sobre un paciente
-- ---------------------------------------------------------------------------
CREATE TABLE nota_compartida (
    id              VARCHAR(40) PRIMARY KEY,
    paciente_id     VARCHAR(40) NOT NULL REFERENCES paciente (id),
    especialidad_id VARCHAR(4)  NOT NULL REFERENCES especialidad (id),
    profesional_id  VARCHAR(40) NOT NULL REFERENCES profesional (id),
    fecha           DATE        NOT NULL,
    texto           TEXT        NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_nota_paciente ON nota_compartida (paciente_id, fecha DESC);
CREATE SEQUENCE nota_compartida_seq START WITH 101;

-- ---------------------------------------------------------------------------
-- Inmutabilidad: una sesión guardada (y sus evaluaciones) no se modifica ni se borra.
-- ---------------------------------------------------------------------------
CREATE FUNCTION fn_registro_inmutable() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Registro de % inmutable: las sesiones guardadas no se modifican ni se eliminan', TG_TABLE_NAME
        USING ERRCODE = 'restrict_violation';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sesion_inmutable
    BEFORE UPDATE OR DELETE ON sesion
    FOR EACH ROW EXECUTE FUNCTION fn_registro_inmutable();

CREATE TRIGGER trg_evaluacion_inmutable
    BEFORE UPDATE OR DELETE ON evaluacion_objetivo
    FOR EACH ROW EXECUTE FUNCTION fn_registro_inmutable();
