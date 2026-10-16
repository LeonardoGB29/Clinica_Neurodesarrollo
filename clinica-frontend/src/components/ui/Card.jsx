/**
 * Tarjeta base del dashboard: fondo blanco, bordes redondeados y sombra suave.
 */
export default function Card({
  id,
  title,
  subtitle,
  icon: Icon,
  actions,
  children,
  className = "",
  bodyClassName = "",
}) {
  return (
    <section id={id} className={`card ${className}`}>
      {(title || actions) && (
        <header className="flex flex-wrap items-start justify-between gap-3 px-5 pt-5 pb-3">
          <div className="flex min-w-0 items-start gap-3">
            {Icon && (
              <span className="mt-0.5 grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-primary-50 text-primary-600">
                <Icon size={18} />
              </span>
            )}
            <div className="min-w-0">
              <h2 className="text-base font-semibold text-navy">{title}</h2>
              {subtitle && <p className="mt-0.5 text-xs text-slate-500">{subtitle}</p>}
            </div>
          </div>
          {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
        </header>
      )}
      <div className={`px-5 pb-5 ${bodyClassName}`}>{children}</div>
    </section>
  );
}
