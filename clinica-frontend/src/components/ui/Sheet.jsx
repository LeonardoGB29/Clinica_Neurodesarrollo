import { useEffect, useRef } from "react";
import { X } from "lucide-react";

/**
 * Panel lateral deslizable ("hoja") que se abre desde el borde derecho sobre la
 * vista actual. Se cierra con el botón, con Esc o haciendo clic fuera.
 * Ancho: toda la pantalla en móvil; hasta 56rem (896 px) en pantallas grandes.
 */
export default function Sheet({
  open,
  onClose,
  title,
  subtitle,
  icon: Icon,
  children,
  widthClassName = "sm:max-w-2xl lg:max-w-3xl xl:max-w-4xl",
}) {
  const closeRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeRef.current?.focus();
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = previousOverflow;
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="no-print fixed inset-0 z-50" role="dialog" aria-modal="true" aria-labelledby="sheet-title">
      <div
        className="absolute inset-0 animate-fade-in bg-navy/40 motion-reduce:animate-none"
        onClick={onClose}
        aria-hidden="true"
      />
      <aside
        className={`absolute inset-y-0 right-0 flex w-full animate-sheet-in flex-col bg-white shadow-2xl motion-reduce:animate-none ${widthClassName}`}
      >
        <header className="flex items-start gap-4 border-b border-slate-100 px-6 py-5 lg:px-8">
          {Icon && (
            <span className="mt-0.5 grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-primary-50 text-primary-600">
              <Icon size={20} />
            </span>
          )}
          <div className="min-w-0 flex-1">
            <h2 id="sheet-title" className="text-lg font-semibold text-navy">
              {title}
            </h2>
            {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
          </div>
          <button
            ref={closeRef}
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-slate-500 transition hover:bg-slate-100 hover:text-navy focus:outline-none focus-visible:ring-4 focus-visible:ring-primary-100"
            aria-label="Cerrar panel"
          >
            <X size={20} />
          </button>
        </header>

        <div className="scrollbar-thin flex-1 overflow-y-auto px-6 py-6 lg:px-8 lg:py-7">{children}</div>

        <footer className="border-t border-slate-100 px-6 py-3 text-xs text-slate-400 lg:px-8">
          Presiona Esc o haz clic fuera del panel para volver a la ficha.
        </footer>
      </aside>
    </div>
  );
}
