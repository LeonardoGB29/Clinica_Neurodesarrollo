/**
 * Endpoints del contrato API (/api/v1). Ver ../../../clinica-backend/README.md
 * y STACK_TECNOLOGICO.md (sección 6). Las respuestas tienen la misma forma que
 * los objetos de src/data/mockData.js, por lo que los componentes no cambian.
 */
import { request, requestBlob } from "./client";

const enc = encodeURIComponent;

// ----------------------------------------------------------------- autenticación
/** POST /auth/login → { token, tipo, expiraEnSegundos, usuario: { id, nombreUsuario, rol, profesionalId, specialtyId } } */
export const login = (usuario, clave) =>
  request("/auth/login", { method: "POST", body: { usuario, clave }, auth: false });

// ----------------------------------------------------------------- catálogos y cabecera
/** GET /profesionales/me → currentProfessional */
export const getProfesionalActual = () => request("/profesionales/me");

/** GET /especialidades → lista con la forma de SPECIALTIES[id] */
export const getEspecialidades = () => request("/especialidades");

/** GET /pacientes/{id} → patient */
export const getPaciente = (pacienteId) => request(`/pacientes/${enc(pacienteId)}`);

// ----------------------------------------------------------------- ciclo
/** GET /pacientes/{id}/ciclos?especialidad=TO → { ...cycle, objectives } (404 si no hay ciclo abierto) */
export const getCicloActivo = (pacienteId, especialidad) =>
  request(`/pacientes/${enc(pacienteId)}/ciclos`, { query: { especialidad } });

/**
 * POST /pacientes/{id}/ciclos {specialtyId, startDate?, frequency?, totalSessions?} → 201 { ...cycle, objectives: [] }
 * (sesión 1, sin objetivos) · 409 si el paciente ya tiene un ciclo abierto de esa especialidad
 */
export const crearCiclo = (pacienteId, body) =>
  request(`/pacientes/${enc(pacienteId)}/ciclos`, { method: "POST", body });

/** GET /ciclos/{id} → { ...cycle, objectives } (también para ciclos cerrados) */
export const getCiclo = (cicloId) => request(`/ciclos/${enc(cicloId)}`);

/** GET /ciclos/{id}/sesiones → sessionHistory */
export const getSesiones = (cicloId) => request(`/ciclos/${enc(cicloId)}/sesiones`);

/** POST /ciclos/{id}/sesiones → sesión registrada (al guardar la 12.ª el backend cierra el ciclo) */
export const registrarSesion = (cicloId, sesion) =>
  request(`/ciclos/${enc(cicloId)}/sesiones`, { method: "POST", body: sesion });

/** GET /ciclos/{id}/reporte.pdf → { blob, filename } */
export const descargarReportePdf = (cicloId) => requestBlob(`/ciclos/${enc(cicloId)}/reporte.pdf`);

// ----------------------------------------------------------------- objetivos
/** GET /banco-objetivos?especialidad=TO → objectiveBank[TO] */
export const getBancoObjetivos = (especialidad) => request("/banco-objetivos", { query: { especialidad } });

/** POST /banco-objetivos {text, specialtyId?} → 201 creado · 200 si ya existía con el mismo texto */
export const crearObjetivoBanco = (text, specialtyId) =>
  request("/banco-objetivos", { method: "POST", body: specialtyId ? { text, specialtyId } : { text } });

/** POST /ciclos/{id}/objetivos {bankId, baseline?, target?} → objetivo activo · 409 si ya hay 3 */
export const activarObjetivo = (cicloId, bankId, extra = {}) =>
  request(`/ciclos/${enc(cicloId)}/objetivos`, { method: "POST", body: { bankId, ...extra } });

/** PATCH /ciclos/{id}/objetivos/{objetivoId} {baseline?, target?} → objetivo actualizado */
export const ajustarObjetivo = (cicloId, objetivoId, { baseline, target }) =>
  request(`/ciclos/${enc(cicloId)}/objetivos/${enc(objetivoId)}`, { method: "PATCH", body: { baseline, target } });

/** DELETE /ciclos/{id}/objetivos/{objetivoId} → 204 */
export const retirarObjetivo = (cicloId, objetivoId) =>
  request(`/ciclos/${enc(cicloId)}/objetivos/${enc(objetivoId)}`, { method: "DELETE" });

// ----------------------------------------------------------------- vista 360° y notas
/** GET /pacientes/{id}/vista-360 → otherSpecialtiesProgress */
export const getVista360 = (pacienteId) => request(`/pacientes/${enc(pacienteId)}/vista-360`);

/** GET /pacientes/{id}/notas → sharedNotes */
export const getNotas = (pacienteId) => request(`/pacientes/${enc(pacienteId)}/notas`);
