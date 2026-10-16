import { CalendarDays, ChartColumn, ClipboardList, LifeBuoy, LogOut, Settings, Users, X } from "lucide-react";
import logo from "../../assets/logo-horizontal.svg";
import Avatar from "../ui/Avatar";

const NAV = [
  { label: "Agenda", icon: CalendarDays },
  { label: "Pacientes", icon: Users },
  { label: "Ficha de evolución", icon: ClipboardList, active: true },
  { label: "Reportes", icon: ChartColumn },
  { label: "Configuración", icon: Settings },
];

function SidebarContent({ professional, specialty, onClose, onLogout }) {
  return (
    <>
      <div className="flex items-start justify-between gap-2 border-b border-slate-100 px-5 py-5">
        <div>
          <img src={logo} alt="Clínica San Juan de Dios" className="h-10 w-auto" />
          <p className="mt-2 text-[11px] font-semibold uppercase tracking-wider text-primary-600">
            Centro de Neurodesarrollo
          </p>
        </div>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 lg:hidden"
            aria-label="Cerrar menú"
          >
            <X size={18} />
          </button>
        )}
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4" aria-label="Navegación principal">
        {NAV.map(({ label, icon: Icon, active }) => (
          <a
            key={label}
            href="#"
            onClick={(e) => e.preventDefault()}
            aria-current={active ? "page" : undefined}
            className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
              active
                ? "bg-primary-50 text-primary-700"
                : "text-slate-600 hover:bg-slate-50 hover:text-navy"
            }`}
          >
            <Icon size={18} className={active ? "text-primary-600" : "text-slate-400"} />
            {label}
          </a>
        ))}
      </nav>

      <div className="px-3 pb-3">
        <a
          href="#"
          onClick={(e) => e.preventDefault()}
          className="mb-2 flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-slate-500 hover:bg-slate-50"
        >
          <LifeBuoy size={18} className="text-slate-400" /> Soporte de Sistemas
        </a>
        <div className="flex items-center gap-3 rounded-xl bg-surface p-3">
          <Avatar initials={professional.initials} size="sm" />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-semibold text-navy">{professional.fullName}</p>
            <p className="truncate text-xs text-slate-500">
              {specialty.short} · {professional.college} {professional.licenseNumber}
            </p>
          </div>
          {onLogout && (
            <button
              type="button"
              onClick={onLogout}
              className="shrink-0 rounded-lg p-1.5 text-slate-400 transition hover:bg-white hover:text-rose-600"
              aria-label="Cerrar sesión"
              title="Cerrar sesión"
            >
              <LogOut size={16} />
            </button>
          )}
        </div>
      </div>
    </>
  );
}

export default function Sidebar({ professional, specialty, open, onClose, onLogout }) {
  return (
    <>
      {/* Escritorio */}
      <aside className="no-print sticky top-0 hidden h-screen w-64 shrink-0 flex-col border-r border-slate-200 bg-white lg:flex">
        <SidebarContent professional={professional} specialty={specialty} onLogout={onLogout} />
      </aside>

      {/* Móvil / tablet: cajón deslizable */}
      {open && (
        <div className="no-print fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-navy/40" onClick={onClose} aria-hidden="true" />
          <aside className="absolute left-0 top-0 flex h-full w-72 flex-col bg-white shadow-xl">
            <SidebarContent professional={professional} specialty={specialty} onClose={onClose} onLogout={onLogout} />
          </aside>
        </div>
      )}
    </>
  );
}
