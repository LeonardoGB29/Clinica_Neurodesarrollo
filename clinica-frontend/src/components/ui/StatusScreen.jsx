import { CircleAlert, LoaderCircle, LogOut, RefreshCw } from "lucide-react";
import logo from "../../assets/logo-horizontal.svg";
import { API_URL, errorMessage } from "../../api/client";
import Button from "./Button";

/** Pantalla de carga de la ficha (mientras se consultan los endpoints de la API). */
export function LoadingScreen({ message = "Cargando la ficha de evolución…" }) {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-5 bg-surface px-4" aria-busy="true">
      <img src={logo} alt="Clínica San Juan de Dios" className="h-10 w-auto" />
      <div className="flex items-center gap-2 text-sm text-slate-600">
        <LoaderCircle size={18} className="animate-spin text-primary-500" />
        {message}
      </div>
    </main>
  );
}

/** Pantalla de error de la carga inicial con reintento y cierre de sesión. */
export function ErrorScreen({ error, onRetry, onLogout }) {
  const title = error?.title ?? "No se pudo cargar la ficha de evolución";
  const detail = errorMessage(error);
  const offline = error?.status === 0;

  return (
    <main className="flex min-h-screen items-center justify-center bg-surface px-4">
      <section className="card w-full max-w-lg p-7 text-center" role="alert">
        <span className="mx-auto grid h-12 w-12 place-items-center rounded-full bg-rose-50 text-rose-600">
          <CircleAlert size={24} />
        </span>
        <h1 className="mt-4 text-lg font-bold text-navy">{title}</h1>
        <p className="mt-2 text-sm leading-relaxed text-slate-600">{detail}</p>
        {offline && (
          <p className="mt-3 rounded-lg bg-surface px-3 py-2 text-left text-xs leading-relaxed text-slate-600">
            La aplicación consulta <code className="font-semibold">{API_URL}</code>. En{" "}
            <code className="font-semibold">clinica-backend/</code> ejecuta{" "}
            <code className="font-semibold">docker compose up -d db</code> y luego{" "}
            <code className="font-semibold">{".\\mvnw.cmd spring-boot:run"}</code>; si la API corre en otra
            dirección, define <code className="font-semibold">VITE_API_URL</code> en un archivo{" "}
            <code className="font-semibold">.env.local</code>.
          </p>
        )}
        <div className="mt-6 flex flex-wrap justify-center gap-2">
          <Button variant="primary" icon={RefreshCw} onClick={onRetry}>
            Reintentar
          </Button>
          <Button variant="outline" icon={LogOut} onClick={onLogout}>
            Cerrar sesión
          </Button>
        </div>
      </section>
    </main>
  );
}
