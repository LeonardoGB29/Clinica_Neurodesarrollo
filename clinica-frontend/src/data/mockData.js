/**
 * ============================================================================
 *  DATOS MOCKEADOS – Prototipo Ficha de Evolución (Centro de Neurodesarrollo)
 * ============================================================================
 *  La aplicación ya NO usa este archivo: el hook useClinicalRecord consume la
 *  API REST (clinica-backend) a través de src/api/. Se conserva como referencia
 *  de la forma exacta de los DTOs (los datos semilla del backend,
 *  V2__datos_semilla.sql, son estos mismos) y como fixture para pruebas.
 *
 *  Convenciones:
 *   - Los porcentajes de sesión se registran como aciertos/intentos
 *     (ej. 3 de 10 veces = 30 %).
 *   - Un ciclo terapéutico dura 12 semanas (12 sesiones) y admite máximo
 *     3 objetivos activos por especialidad.
 * ============================================================================
 */

/** Especialidades del centro (colegios profesionales del Perú) */
export const SPECIALTIES = {
  TO: {
    id: "TO",
    name: "Terapia Ocupacional",
    short: "T. Ocupacional",
    college: "CTMP",
    color: "#0083A8",
  },
  TL: {
    id: "TL",
    name: "Terapia de Lenguaje",
    short: "T. Lenguaje",
    college: "CTMP",
    color: "#6F4FB3",
  },
  TF: {
    id: "TF",
    name: "Terapia Física",
    short: "T. Física",
    college: "CTMP",
    color: "#7F9A1C",
  },
  PS: {
    id: "PS",
    name: "Psicología",
    short: "Psicología",
    college: "CPsP",
    color: "#C2185B",
  },
};

/** Profesional logueado (datos precargados desde la sesión de autenticación) */
export const currentProfessional = {
  id: "prof-017",
  fullName: "Lic. María Fernanda Quispe Rojas",
  initials: "MQ",
  specialtyId: "TO",
  college: "CTMP",
  licenseNumber: "14582",
  email: "mquispe@clinicasanjuandedios.pe",
};

/** Paciente de la ficha actual */
export const patient = {
  id: "pac-0231",
  historyNumber: "HC-2024-0231",
  fullName: "Mateo Alejandro Huamán Torres",
  initials: "MH",
  birthDate: "2019-03-12",
  sex: "M",
  diagnoses: [
    "TEA nivel 1 (CIE-11 6A02.0)",
    "TDAH presentación combinada (CIE-11 6A05.2)",
  ],
  guardian: { name: "Rosa Torres Villanueva", relation: "Madre", phone: "987 654 321" },
  coverage: "Salud Card · Convenio corporativo",
  school: "I.E. Santa Rosa · 1.er grado",
};

/** Ciclo terapéutico vigente para la especialidad del profesional logueado */
export const cycle = {
  id: "cic-2026-TO-0231-03",
  specialtyId: "TO",
  startDate: "2026-09-24",
  totalSessions: 12,
  currentSession: 4,
  frequency: "1 sesión / semana · 45 min",
  maxObjectives: 3,
  status: "open",
};

/**
 * Banco de objetivos por especialidad.
 * Cada objetivo tiene un id estable para poder reutilizarse en futuros ciclos.
 */
export const objectiveBank = {
  TO: [
    { id: "to-001", text: "Agarrar el lápiz con pinza trípode" },
    { id: "to-002", text: "Mantener la atención en una actividad de mesa durante 10 minutos" },
    { id: "to-003", text: "Abotonar y desabotonar prendas de forma autónoma" },
    { id: "to-004", text: "Tolerar texturas diversas en actividades de integración sensorial" },
    { id: "to-005", text: "Recortar con tijera siguiendo una línea recta" },
    { id: "to-006", text: "Enhebrar cuentas medianas en un cordón" },
    { id: "to-007", text: "Copiar figuras geométricas básicas (círculo, cuadrado, triángulo)" },
    { id: "to-008", text: "Vestirse y desvestirse con mínima asistencia" },
    { id: "to-009", text: "Cruzar la línea media en actividades bimanuales" },
    { id: "to-010", text: "Seguir rutinas de 3 pasos con apoyo visual" },
  ],
  TL: [
    { id: "tl-001", text: "Producir el fonema /r/ en posición inicial de palabra" },
    { id: "tl-002", text: "Responder preguntas simples (¿qué?, ¿quién?, ¿dónde?)" },
    { id: "tl-003", text: "Ampliar la longitud media del enunciado a 3-4 palabras" },
    { id: "tl-004", text: "Mantener contacto visual al iniciar la comunicación" },
    { id: "tl-005", text: "Usar fórmulas sociales (hola, gracias, por favor) de forma espontánea" },
    { id: "tl-006", text: "Seguir instrucciones de dos pasos sin apoyo gestual" },
    { id: "tl-007", text: "Nombrar objetos de categorías semánticas (animales, alimentos)" },
    { id: "tl-008", text: "Narrar secuencias de 3 láminas con coherencia temporal" },
  ],
  TF: [
    { id: "tf-001", text: "Mantener el equilibrio monopodal durante 5 segundos" },
    { id: "tf-002", text: "Saltar con ambos pies sin apoyo" },
    { id: "tf-003", text: "Subir y bajar escaleras alternando pies" },
    { id: "tf-004", text: "Lanzar y atrapar una pelota mediana a 2 metros" },
    { id: "tf-005", text: "Mantener postura sedente erguida durante 10 minutos" },
    { id: "tf-006", text: "Caminar sobre una línea recta (talón-punta)" },
    { id: "tf-007", text: "Completar un circuito motor de 4 estaciones" },
  ],
  PS: [
    { id: "ps-001", text: "Identificar y nombrar emociones básicas en sí mismo" },
    { id: "ps-002", text: "Tolerar la frustración ante cambios de rutina" },
    { id: "ps-003", text: "Esperar su turno en juegos estructurados" },
    { id: "ps-004", text: "Reducir conductas de evitación en interacciones con pares" },
    { id: "ps-005", text: "Usar estrategias de autorregulación (respiración, rincón de calma)" },
    { id: "ps-006", text: "Iniciar interacciones de juego con un par" },
    { id: "ps-007", text: "Cumplir límites con apoyo de economía de fichas" },
  ],
};

/**
 * Objetivos activos del ciclo (máx. 3).
 *  - baseline: % de éxito en la evaluación inicial (línea base).
 *  - target:   % esperado al cierre del ciclo (nivel 0 de la escala GAS).
 */
export const activeObjectives = [
  {
    id: "obj-1",
    bankId: "to-001",
    text: "Agarrar el lápiz con pinza trípode",
    baseline: 10,
    target: 80,
    addedInSession: 1,
  },
  {
    id: "obj-2",
    bankId: "to-002",
    text: "Mantener la atención en una actividad de mesa durante 10 minutos",
    baseline: 20,
    target: 70,
    addedInSession: 1,
  },
];

/**
 * Historial de sesiones ya registradas (solo lectura).
 * evaluations[objectiveId] = { hits, total }  →  % = hits / total
 */
export const sessionHistory = [
  {
    number: 1,
    date: "2026-09-24",
    professionalName: "Lic. María Fernanda Quispe Rojas",
    evaluations: {
      "obj-1": { hits: 2, total: 10 },
      "obj-2": { hits: 1, total: 5 },
    },
    activity:
      "Evaluación de línea base de la prensión. Actividades de pinza con pinzas de ropa y plastilina; trazos libres con crayón grueso. Se introdujo el tablero de rutina visual (3 pasos) para la actividad de mesa.",
    achievement:
      "Logra pinza digital inferior. La pinza trípode aparece de forma emergente solo con guía física total. Permanece sentado 3-4 minutos antes de levantarse.",
    observations:
      "Baja tolerancia a la frustración al corregir el agarre. Se recomienda en casa: juegos de pinza (pinzas de ropa, cuentas grandes) 10 minutos diarios. Coordinar con Psicología estrategias de regulación.",
  },
  {
    number: 2,
    date: "2026-10-01",
    professionalName: "Lic. María Fernanda Quispe Rojas",
    evaluations: {
      "obj-1": { hits: 3, total: 10 },
      "obj-2": { hits: 2, total: 6 },
    },
    activity:
      "Uso de adaptador triangular en el lápiz. Trazos verticales y horizontales en plano vertical (pizarra). Actividad de mesa con reloj visual y economía de fichas (1 ficha por cada 2 minutos sentado).",
    achievement:
      "Con adaptador logra pinza trípode en 3 de 10 intentos sin ayuda física. Tolera 5 minutos continuos en la actividad de mesa con 2 recordatorios verbales.",
    observations:
      "Mejor respuesta cuando la consigna va acompañada de apoyo visual. Mantener el adaptador en casa y en el colegio. Reducir progresivamente la guía física.",
  },
  {
    number: 3,
    date: "2026-10-08",
    professionalName: "Lic. María Fernanda Quispe Rojas",
    evaluations: {
      "obj-1": { hits: 5, total: 10 },
      "obj-2": { hits: 3, total: 6 },
    },
    activity:
      "Trazos de pre-escritura (líneas onduladas y en zigzag) con lápiz adaptado. Juego de ensartado con cuentas medianas. Actividad de mesa de 8 minutos dividida en dos bloques con pausa sensorial (cojín vibratorio).",
    achievement:
      "Pinza trípode funcional en 5 de 10 intentos con retiro de la guía física; aún requiere recordatorio verbal. Completa el primer bloque de 4 minutos sin levantarse.",
    observations:
      "La pausa sensorial entre bloques mejora la permanencia en la tarea. Se sugiere a Terapia de Lenguaje reforzar el seguimiento de instrucciones de dos pasos en mesa. Enviar hoja de trazos para casa.",
  },
];

/**
 * Vista 360°: objetivos y progreso del paciente en las otras especialidades.
 * Solo lectura para el profesional actual.
 *  - history: % de éxito por sesión registrada (para el sparkline).
 */
export const otherSpecialtiesProgress = [
  {
    specialtyId: "TL",
    professionalName: "Lic. Carla Mendoza Paredes",
    license: "CTMP 21877",
    currentSession: 5,
    totalSessions: 12,
    lastSessionDate: "2026-10-13",
    objectives: [
      {
        id: "tl-obj-1",
        text: "Ampliar la longitud media del enunciado a 3-4 palabras",
        target: 75,
        history: [20, 30, 40, 50, 55],
      },
      {
        id: "tl-obj-2",
        text: "Responder preguntas simples (¿qué?, ¿quién?, ¿dónde?)",
        target: 80,
        history: [40, 50, 60, 70, 70],
      },
      {
        id: "tl-obj-3",
        text: "Mantener contacto visual al iniciar la comunicación",
        target: 70,
        history: [25, 30, 30, 40, 45],
      },
    ],
    lastNote:
      "Responde mejor en formato de juego simbólico. Se recomienda a las demás áreas usar consignas cortas y esperar 5 segundos antes de repetir.",
  },
  {
    specialtyId: "TF",
    professionalName: "Lic. Jorge Luis Salazar Peña",
    license: "CTMP 19034",
    currentSession: 3,
    totalSessions: 12,
    lastSessionDate: "2026-10-09",
    objectives: [
      {
        id: "tf-obj-1",
        text: "Mantener el equilibrio monopodal durante 5 segundos",
        target: 80,
        history: [20, 30, 40],
      },
      {
        id: "tf-obj-2",
        text: "Saltar con ambos pies sin apoyo",
        target: 90,
        history: [50, 60, 70],
      },
    ],
    lastNote:
      "Hipotonía leve de tronco; se trabaja control postural. La postura sedente erguida impacta directamente en la escritura (coordinar con T. Ocupacional).",
  },
  {
    specialtyId: "PS",
    professionalName: "Ps. Andrea Castillo Ruiz",
    license: "CPsP 28410",
    currentSession: 6,
    totalSessions: 12,
    lastSessionDate: "2026-10-14",
    objectives: [
      {
        id: "ps-obj-1",
        text: "Tolerar la frustración ante cambios de rutina",
        target: 70,
        history: [10, 20, 20, 30, 35, 40],
      },
      {
        id: "ps-obj-2",
        text: "Esperar su turno en juegos estructurados",
        target: 80,
        history: [30, 40, 45, 50, 60, 60],
      },
      {
        id: "ps-obj-3",
        text: "Usar estrategias de autorregulación (respiración, rincón de calma)",
        target: 75,
        history: [15, 25, 35, 40, 50, 55],
      },
    ],
    lastNote:
      "Se implementó rincón de calma con pictogramas; la familia lo replica en casa. Sugerencia a todas las áreas: anticipar el cambio de actividad con cuenta regresiva visual.",
  },
];

/** Notas interdisciplinarias compartidas (lectura) */
export const sharedNotes = [
  {
    id: "note-1",
    specialtyId: "PS",
    date: "2026-10-14",
    text: "Nuevo plan de refuerzo: economía de fichas unificada para todas las terapias (1 ficha = 2 min en tarea).",
  },
  {
    id: "note-2",
    specialtyId: "TF",
    date: "2026-10-09",
    text: "Usar cojín de aire en la silla durante actividades de mesa para favorecer el control postural.",
  },
  {
    id: "note-3",
    specialtyId: "TL",
    date: "2026-10-06",
    text: "Evitar consignas de más de 2 pasos; apoyar con pictogramas de secuencia.",
  },
];

export const mockData = {
  SPECIALTIES,
  currentProfessional,
  patient,
  cycle,
  objectiveBank,
  activeObjectives,
  sessionHistory,
  otherSpecialtiesProgress,
  sharedNotes,
};

export default mockData;
