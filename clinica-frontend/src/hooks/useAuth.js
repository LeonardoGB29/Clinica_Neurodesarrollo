import { useCallback, useEffect, useState } from "react";
import * as api from "../api/clinicaApi";
import { onUnauthorized } from "../api/client";
import { clearSession, readSession, saveSession } from "../api/session";

/**
 * Sesión del usuario autenticado (JWT en sessionStorage).
 *  - login(usuario, clave): POST /auth/login y guarda token + usuario (profesionalId, specialtyId).
 *  - logout(): descarta la sesión.
 *  - Si cualquier petición recibe 401, la sesión se cierra sola y se muestra un aviso en el login.
 */
export function useAuth() {
  const [session, setSession] = useState(readSession);
  const [notice, setNotice] = useState(null);

  useEffect(
    () =>
      onUnauthorized(() => {
        setSession(null);
        setNotice("Tu sesión venció o no es válida. Inicia sesión nuevamente.");
      }),
    []
  );

  const login = useCallback(async (usuario, clave) => {
    const response = await api.login(usuario, clave);
    saveSession(response);
    setNotice(null);
    setSession(readSession() ?? { token: response.token, usuario: response.usuario, expiresAt: 0 });
    return response.usuario;
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setNotice(null);
    setSession(null);
  }, []);

  return { session, usuario: session?.usuario ?? null, notice, login, logout };
}
