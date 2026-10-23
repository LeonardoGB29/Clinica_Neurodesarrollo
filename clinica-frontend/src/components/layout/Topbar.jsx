import { Bell, ChevronRight, Menu, Orbit, Search } from "lucide-react";
import Avatar from "../ui/Avatar";

export default function Topbar({ professional, specialty, patient, onMenu, onOpen360 }) {
  return (
    <header className="no-print sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-slate-200 bg-white/90 px-4 backdrop-blur lg:px-6">
      <button
        type="button"
        onClick={onMenu}
        className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
        aria-label="Abrir menú"
      >
        <Menu size={20} />
      </button>

      <nav className="hidden min-w-0 items-center gap-1 text-sm text-slate-500 md:flex" aria-label="Ruta">
        <span>Pacientes</span>
        <ChevronRight size={14} className="text-slate-300" />
        <span className="truncate">{patient.fullName}</span>
        <ChevronRight size={14} className="text-slate-300" />
        <span className="font-semibold text-navy">Ficha de evolución</span>
      </nav>

      <div className="ml-auto flex items-center gap-2 sm:gap-3">
        <label className="relative hidden md:block">
          <Search size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="search"
            placeholder="Buscar paciente, HC…"
            className="input h-9 w-44 pl-9 xl:w-56"
            aria-label="Buscar paciente"
          />
        </label>
        {onOpen360 && (
          <button
            type="button"
            onClick={onOpen360}
            className="inline-flex h-9 items-center gap-1.5 rounded-lg border border-primary-200 bg-primary-50 px-3 text-xs font-semibold text-primary-700 transition hover:bg-primary-100 focus:outline-none focus-visible:ring-4 focus-visible:ring-primary-100"
            title="Ver el progreso del paciente en las demás especialidades"
          >
            <Orbit size={15} />
            <span className="hidden sm:inline">Vista 360°</span>
          </button>
        )}
        <button
          type="button"
          className="relative rounded-lg p-2 text-slate-500 hover:bg-slate-100"
          aria-label="Notificaciones"
        >
          <Bell size={18} />
          <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-accent-500 ring-2 ring-white" />
        </button>
        <div className="flex items-center gap-2 rounded-full border border-slate-200 p-1 xl:pr-3">
          <Avatar initials={professional.initials} size="sm" />
          <div className="hidden leading-tight xl:block">
            <p className="whitespace-nowrap text-xs font-semibold text-navy">{professional.fullName}</p>
            <p className="whitespace-nowrap text-[11px] text-slate-500">
              {specialty.short} · {professional.college} {professional.licenseNumber}
            </p>
          </div>
        </div>
      </div>
    </header>
  );
}
