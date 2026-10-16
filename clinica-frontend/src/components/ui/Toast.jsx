import { useCallback, useState } from "react";
import { CircleAlert, CircleCheck, Info, X } from "lucide-react";

/** Notificaciones ligeras para confirmar acciones (guardar, agregar objetivo, etc.). */
export function useToast() {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback(
    (message, type = "success") => {
      const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
      setToasts((prev) => [...prev, { id, message, type }]);
      window.setTimeout(() => dismiss(id), 4500);
    },
    [dismiss]
  );

  return { toasts, push, dismiss };
}

const icons = { success: CircleCheck, error: CircleAlert, info: Info };
const tones = {
  success: "border-emerald-200 text-emerald-800",
  error: "border-rose-200 text-rose-800",
  info: "border-primary-200 text-primary-800",
};

export function ToastStack({ toasts, onDismiss }) {
  return (
    <div className="no-print pointer-events-none fixed bottom-5 right-5 z-50 flex w-80 max-w-[calc(100vw-2.5rem)] flex-col gap-2">
      {toasts.map((t) => {
        const Icon = icons[t.type] ?? Info;
        return (
          <div
            key={t.id}
            role="status"
            className={`pointer-events-auto flex items-start gap-2 rounded-xl border bg-white px-3 py-2.5 text-sm shadow-card-hover ${tones[t.type] ?? tones.info}`}
          >
            <Icon size={16} className="mt-0.5 shrink-0" />
            <span className="flex-1">{t.message}</span>
            <button
              type="button"
              onClick={() => onDismiss(t.id)}
              className="text-slate-400 transition hover:text-slate-600"
              aria-label="Cerrar notificación"
            >
              <X size={14} />
            </button>
          </div>
        );
      })}
    </div>
  );
}
