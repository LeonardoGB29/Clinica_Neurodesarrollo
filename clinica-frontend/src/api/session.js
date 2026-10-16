/**
 * Sesión de autenticación en el navegador.
 * El JWT y los datos mínimos del usuario (profesionalId, specialtyId) se guardan en
 * sessionStorage: sobreviven a recargas de la pestaña y se descartan al cerrarla.
 */
const TOKEN_KEY = "clinica.token";
const USER_KEY = "clinica.usuario";
const EXPIRES_KEY = "clinica.expira";

/** Sesión vigente o null si no hay token (o ya venció). */
export function readSession() {
  try {
    const token = sessionStorage.getItem(TOKEN_KEY);
    const rawUser = sessionStorage.getItem(USER_KEY);
    if (!token || !rawUser) return null;
    const expiresAt = Number(sessionStorage.getItem(EXPIRES_KEY)) || 0;
    if (expiresAt && expiresAt <= Date.now()) {
      clearSession();
      return null;
    }
    return { token, usuario: JSON.parse(rawUser), expiresAt };
  } catch {
    return null;
  }
}

/** Guarda la respuesta de POST /auth/login ({token, expiraEnSegundos, usuario}). */
export function saveSession({ token, expiraEnSegundos, usuario }) {
  try {
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(USER_KEY, JSON.stringify(usuario ?? {}));
    const expiresAt = expiraEnSegundos ? Date.now() + Number(expiraEnSegundos) * 1000 : 0;
    sessionStorage.setItem(EXPIRES_KEY, String(expiresAt));
  } catch {
    /* sessionStorage no disponible (modo privado estricto): la sesión vive solo en memoria */
  }
}

export function clearSession() {
  try {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(EXPIRES_KEY);
  } catch {
    /* ignorar */
  }
}

export function getToken() {
  return readSession()?.token ?? null;
}
