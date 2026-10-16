/**
 * ============================================================================
 *  REGLAS DE CÁLCULO DE PROGRESO Y ESCALA GAS
 * ============================================================================
 *  Centralizadas aquí para que el área clínica pueda validar/ajustar la
 *  metodología sin tocar los componentes de UI. En producción estas reglas
 *  vivirán en el backend (servicio de dominio) y el front solo las mostrará.
 * ============================================================================
 */

/** % de éxito de una evaluación: aciertos / intentos (ej. 3 de 10 = 30 %). */
export function successRate(evaluation) {
  if (!evaluation) return null;
  if (evaluation.hits === "" || evaluation.total === "") return null;
  const hits = Number(evaluation.hits);
  const total = Number(evaluation.total);
  if (!Number.isFinite(hits) || !Number.isFinite(total) || total <= 0 || hits < 0) return null;
  return Math.round(Math.min(hits / total, 1) * 100);
}

/** Serie de % por sesión para un objetivo (null cuando no se evaluó). */
export function scoreSeries(sessions, objectiveId) {
  return sessions.map((s) => ({
    session: s.number,
    date: s.date,
    value: successRate(s.evaluations?.[objectiveId]),
  }));
}

function recordedValues(sessions, objectiveId) {
  return scoreSeries(sessions, objectiveId)
    .map((p) => p.value)
    .filter((v) => v !== null);
}

/** Último % registrado (nivel actual del objetivo). */
export function lastScore(sessions, objectiveId) {
  const values = recordedValues(sessions, objectiveId);
  return values.length ? values[values.length - 1] : null;
}

/**
 * % de avance acumulado del objetivo en el ciclo.
 * Regla actual: promedio de la tasa de éxito de las sesiones registradas.
 * (Alternativas a validar con el equipo clínico: último valor, mejor valor,
 *  o avance relativo entre línea base y meta).
 */
export function accumulatedProgress(sessions, objectiveId) {
  const values = recordedValues(sessions, objectiveId);
  if (!values.length) return 0;
  return Math.round(values.reduce((a, b) => a + b, 0) / values.length);
}

/** Variación entre las dos últimas sesiones registradas (puntos porcentuales). */
export function lastDelta(sessions, objectiveId) {
  const values = recordedValues(sessions, objectiveId);
  if (values.length < 2) return null;
  return values[values.length - 1] - values[values.length - 2];
}

/** Cantidad de sesiones en las que el objetivo fue evaluado. */
export function recordedCount(sessions, objectiveId) {
  return recordedValues(sessions, objectiveId).length;
}

/**
 * Escala GAS (Goal Attainment Scaling)
 *   +2  Mucho mejor de lo esperado  → subir carga / nuevo objetivo
 *   +1  Mejor de lo esperado        → subir carga
 *    0  Resultado esperado (meta)   → mantener
 *   -1  Menor de lo esperado        → mantener con ajustes
 *   -2  Sin cambios / retroceso     → bajar carga
 */
export const GAS_LEVELS = [
  { level: -2, label: "Sin cambios", decision: "Bajar carga", tone: "danger" },
  { level: -1, label: "Menor a lo esperado", decision: "Mantener con ajustes", tone: "warning" },
  { level: 0, label: "Resultado esperado", decision: "Mantener", tone: "neutral" },
  { level: 1, label: "Mejor a lo esperado", decision: "Subir carga", tone: "success" },
  { level: 2, label: "Mucho mejor", decision: "Subir carga / nuevo objetivo", tone: "success" },
];

/**
 * Calcula el nivel GAS a partir del % alcanzado, la línea base y la meta.
 * Bandas (ajustables con el equipo clínico):
 *   score ≥ meta + 15 → +2 | score ≥ meta + 5 → +1 | score ≥ meta − 10 → 0
 *   score > línea base → −1 | score ≤ línea base → −2
 */
export function computeGas(score, baseline, target) {
  if (score === null || score === undefined) return null;
  let level;
  if (score >= target + 15) level = 2;
  else if (score >= target + 5) level = 1;
  else if (score >= target - 10) level = 0;
  else if (score > baseline) level = -1;
  else level = -2;
  return GAS_LEVELS.find((g) => g.level === level);
}

/** El GAS definitivo se habilita en la última sesión del ciclo (semana 12). */
export function isCycleClosing(cycle) {
  return cycle.status === "closed" || cycle.currentSession >= cycle.totalSessions;
}

/** Edad en años a partir de la fecha de nacimiento (YYYY-MM-DD). */
export function ageFromBirthDate(birthDate, today = new Date()) {
  const b = new Date(`${birthDate}T00:00:00`);
  let age = today.getFullYear() - b.getFullYear();
  const m = today.getMonth() - b.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < b.getDate())) age -= 1;
  return age;
}

/** Fecha corta en español: 24 sep 2026 */
export function formatDate(iso) {
  if (!iso) return "";
  const d = new Date(`${iso}T00:00:00`);
  return d.toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" });
}

/** Fecha ISO de hoy (YYYY-MM-DD) para el formulario de la sesión actual. */
export function todayIso() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/** Tono semántico según el % (se usa en barras y anillos). */
export function toneForScore(score) {
  if (score === null || score === undefined) return "muted";
  if (score >= 70) return "success";
  if (score >= 40) return "primary";
  return "warning";
}
