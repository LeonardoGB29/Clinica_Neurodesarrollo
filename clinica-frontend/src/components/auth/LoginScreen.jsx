import { useState } from "react";
import { CircleAlert, Eye, EyeOff, Info, LockKeyhole, LogIn, UserRound } from "lucide-react";
import logo from "../../assets/logo-vertical.svg";
import { API_URL, errorMessage } from "../../api/client";
import Button from "../ui/Button";

/** Usuarios semilla del backend (solo se muestran en desarrollo). Clave para todos: clinica2026. */
const TEST_USERS = [
  { usuario: "mquispe", rol: "T. Ocupacional" },
  { usuario: "cmendoza", rol: "T. Lenguaje" },
  { usuario: "jsalazar", rol: "T. Física" },
  { usuario: "acastillo", rol: "Psicología" },
];
const TEST_PASSWORD = "clinica2026";

/**
 * Pantalla de inicio de sesión mínima: POST /auth/login {usuario, clave}.
 * Los errores de la API (401 "Usuario o contraseña incorrectos.", sin conexión…) se muestran debajo del formulario.
 */
export default function LoginScreen({ onLogin, notice }) {
  const [usuario, setUsuario] = useState("");
  const [clave, setClave] = useState("");
  const [showClave, setShowClave] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      await onLogin(usuario.trim(), clave);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const fillTestUser = (name) => {
    setUsuario(name);
    setClave(TEST_PASSWORD);
    setError(null);
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-surface px-4 py-10">
      <div className="w-full max-w-md">
        <section className="card overflow-hidden">
          <div className="h-1.5 bg-linear-to-r from-primary-500 via-primary-300 to-accent-500" />
          <div className="p-7 sm:p-8">
            <img src={logo} alt="Clínica San Juan de Dios" className="mx-auto h-20 w-auto" />
            <p className="mt-3 text-center text-[11px] font-semibold uppercase tracking-wider text-primary-600">
              Centro de Neurodesarrollo
            </p>
            <h1 className="mt-4 text-center text-xl font-bold text-navy">Iniciar sesión</h1>
            <p className="mt-1 text-center text-sm text-slate-500">
              Sistema de Gestión Clínica Interdisciplinaria · Ficha de evolución
            </p>

            {notice && (
              <div
                role="status"
                className="mt-5 flex items-start gap-2 rounded-lg border border-accent-100 bg-accent-50 px-3 py-2 text-xs text-accent-700"
              >
                <Info size={14} className="mt-0.5 shrink-0" />
                {notice}
              </div>
            )}

            <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate>
              <div>
                <label className="label" htmlFor="login-usuario">
                  Usuario
                </label>
                <div className="relative">
                  <UserRound
                    size={15}
                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                  />
                  <input
                    id="login-usuario"
                    name="usuario"
                    className="input pl-9"
                    autoComplete="username"
                    autoCapitalize="none"
                    autoFocus
                    placeholder="Usuario institucional"
                    value={usuario}
                    onChange={(e) => setUsuario(e.target.value)}
                  />
                </div>
              </div>

              <div>
                <label className="label" htmlFor="login-clave">
                  Contraseña
                </label>
                <div className="relative">
                  <LockKeyhole
                    size={15}
                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                  />
                  <input
                    id="login-clave"
                    name="clave"
                    type={showClave ? "text" : "password"}
                    className="input pl-9 pr-10"
                    autoComplete="current-password"
                    placeholder="••••••••"
                    value={clave}
                    onChange={(e) => setClave(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowClave((v) => !v)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-1 text-slate-400 transition hover:text-slate-600"
                    aria-label={showClave ? "Ocultar contraseña" : "Mostrar contraseña"}
                  >
                    {showClave ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              {error && (
                <p
                  role="alert"
                  className="flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700"
                >
                  <CircleAlert size={14} className="mt-0.5 shrink-0" />
                  {error}
                </p>
              )}

              <Button
                type="submit"
                variant="primary"
                size="lg"
                icon={LogIn}
                className="w-full"
                disabled={submitting || !usuario.trim() || !clave}
              >
                {submitting ? "Ingresando…" : "Ingresar"}
              </Button>
            </form>

            {import.meta.env.DEV && (
              <div className="mt-6 border-t border-slate-100 pt-4">
                <p className="text-[11px] font-semibold uppercase tracking-wide text-slate-400">
                  Usuarios de prueba · clave {TEST_PASSWORD}
                </p>
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {TEST_USERS.map((u) => (
                    <button
                      key={u.usuario}
                      type="button"
                      onClick={() => fillTestUser(u.usuario)}
                      className="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-[11px] font-medium text-slate-600 transition hover:border-primary-300 hover:text-primary-700"
                    >
                      {u.usuario} · {u.rol}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </section>
        <p className="mt-4 text-center text-[11px] text-slate-400">API: {API_URL}</p>
      </div>
    </main>
  );
}
