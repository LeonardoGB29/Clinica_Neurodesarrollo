/**
 * ============================================================================
 *  CLIENTE HTTP – API REST del Sistema de Gestión Clínica (Spring Boot)
 * ============================================================================
 *  - Base URL configurable con VITE_API_URL (por defecto http://localhost:8080/api/v1).
 *  - Adjunta el JWT guardado en sessionStorage (Authorization: Bearer <token>).
 *  - Convierte las respuestas de error Problem Details (RFC 9457) del backend en
 *    ApiError con title/detail en español, listos para mostrarse en un Toast.
 *  - Ante un 401 limpia la sesión y avisa a la app para volver al login.
 * ============================================================================
 */
import { clearSession, getToken } from "./session";

const DEFAULT_API_URL = "http://localhost:8080/api/v1";

/** URL base de la API sin barra final (p. ej. http://localhost:8080/api/v1 o /api/v1 detrás de Nginx). */
export const API_URL = String(import.meta.env.VITE_API_URL || DEFAULT_API_URL).replace(/\/+$/, "");

/** Error de la API con los campos de Problem Details (status, title, detail, errores por campo). */
export class ApiError extends Error {
  constructor({ status, title, detail, errores, problem, cause }) {
    const camposInvalidos = errores && typeof errores === "object" ? Object.values(errores).join(" ") : "";
    super([detail, camposInvalidos].filter(Boolean).join(" ") || title || `Error HTTP ${status}`);
    this.name = "ApiError";
    this.status = status;
    this.title = title;
    this.detail = detail;
    this.errores = errores ?? null;
    this.problem = problem ?? null;
    if (cause) this.cause = cause;
  }
}

/** Mensaje en español para mostrar al usuario a partir de cualquier error. */
export function errorMessage(err) {
  if (!err) return "Ocurrió un error inesperado.";
  if (err instanceof ApiError) return err.message;
  if (err instanceof Error && err.message) return err.message;
  return String(err);
}

// --------------------------------------------------------------------------- 401 → login
const unauthorizedListeners = new Set();

/** Suscribe un listener que se ejecuta cuando la API responde 401 (token ausente, inválido o vencido). */
export function onUnauthorized(listener) {
  unauthorizedListeners.add(listener);
  return () => unauthorizedListeners.delete(listener);
}

function notifyUnauthorized(error) {
  clearSession();
  unauthorizedListeners.forEach((listener) => listener(error));
}

// --------------------------------------------------------------------------- utilidades
function buildUrl(path, query) {
  const absolute = `${API_URL}${path.startsWith("/") ? path : `/${path}`}`;
  const url = new URL(absolute, window.location.origin);
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") url.searchParams.set(key, String(value));
    });
  }
  return url.toString();
}

function buildHeaders({ auth, hasBody, accept }) {
  const headers = { Accept: accept };
  if (hasBody) headers["Content-Type"] = "application/json";
  const token = getToken();
  if (auth && token) headers.Authorization = `Bearer ${token}`;
  return headers;
}

async function parseBody(res) {
  if (res.status === 204) return null;
  const type = res.headers.get("content-type") || "";
  if (type.includes("json")) return res.json().catch(() => null);
  const text = await res.text().catch(() => "");
  return text || null;
}

function toApiError(res, body) {
  const problem = body && typeof body === "object" ? body : null;
  return new ApiError({
    status: res.status,
    title: problem?.title ?? `Error ${res.status}`,
    detail:
      problem?.detail ??
      (typeof body === "string" && body ? body : `La API respondió con el estado HTTP ${res.status}.`),
    errores: problem?.errores,
    problem,
  });
}

function connectionError(cause) {
  return new ApiError({
    status: 0,
    title: "Sin conexión con la API",
    detail: `No se pudo conectar con ${API_URL}. Verifica que el backend esté en ejecución.`,
    cause,
  });
}

async function send(url, init) {
  try {
    return await fetch(url, init);
  } catch (cause) {
    if (cause?.name === "AbortError") throw cause;
    throw connectionError(cause);
  }
}

// --------------------------------------------------------------------------- API pública

/**
 * Petición JSON a la API. Devuelve el cuerpo parseado (null en 204) o lanza ApiError.
 * @param {string} path  Ruta relativa a API_URL, p. ej. "/ciclos/cic-1/sesiones".
 * @param {object} [options]
 * @param {string} [options.method]  GET por defecto.
 * @param {object} [options.body]    Se serializa como JSON.
 * @param {object} [options.query]   Parámetros de consulta.
 * @param {boolean} [options.auth]   false para no exigir token ni tratar el 401 como sesión vencida (login).
 * @param {AbortSignal} [options.signal]
 */
export async function request(path, { method = "GET", body, query, auth = true, signal } = {}) {
  const hasBody = body !== undefined;
  const res = await send(buildUrl(path, query), {
    method,
    headers: buildHeaders({ auth, hasBody, accept: "application/json, application/problem+json" }),
    body: hasBody ? JSON.stringify(body) : undefined,
    signal,
  });
  const parsed = await parseBody(res);
  if (res.ok) return parsed;

  const error = toApiError(res, parsed);
  if (res.status === 401 && auth) notifyUnauthorized(error);
  throw error;
}

/** Descarga un recurso binario (p. ej. el reporte PDF) con el token. Devuelve { blob, filename }. */
export async function requestBlob(path, { query, accept = "application/pdf", signal } = {}) {
  const res = await send(buildUrl(path, query), {
    method: "GET",
    headers: buildHeaders({ auth: true, hasBody: false, accept: `${accept}, application/problem+json` }),
    signal,
  });
  if (!res.ok) {
    const error = toApiError(res, await parseBody(res));
    if (res.status === 401) notifyUnauthorized(error);
    throw error;
  }
  const disposition = res.headers.get("content-disposition") || "";
  const match = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposition);
  return { blob: await res.blob(), filename: match ? decodeURIComponent(match[1]) : null };
}
