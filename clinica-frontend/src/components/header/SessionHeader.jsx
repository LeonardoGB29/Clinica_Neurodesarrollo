import {
  CalendarCheck,
  CalendarPlus,
  FileDown,
  GraduationCap,
  IdCard,
  Phone,
  Save,
  ShieldCheck,
  Stethoscope,
} from "lucide-react";
import Avatar from "../ui/Avatar";
import Badge from "../ui/Badge";
import Button from "../ui/Button";
import { ageFromBirthDate, formatDate } from "../../utils/progress";

/** Indicador de las 12 sesiones del ciclo (hechas / actual / pendientes). */
function SessionStepper({ total, current, closed }) {
  return (
    <div className="flex items-center gap-1" role="img" aria-label={`Sesión ${current} de ${total}`}>
      {Array.from({ length: total }, (_, i) => {
        const n = i + 1;
        const done = closed || n < current;
        const isCurrent = !closed && n === current;
        return (
          <span
            key={n}
            title={`Sesión ${n}${done ? " · registrada" : isCurrent ? " · en curso" : " · pendiente"}`}
            className={`h-2 flex-1 rounded-full transition ${
              done ? "bg-primary-500" : isCurrent ? "bg-accent-500 ring-2 ring-accent-100" : "bg-slate-200"
            }`}
          />
        );
      })}
    </div>
  );
}

export default function SessionHeader({
  professional,
  specialty,
  patient,
  cycle,
  draft,
  cycleClosed,
  onGeneratePdf,
  onSave,
  onOpenNextCycle,
}) {
  const age = ageFromBirthDate(patient.birthDate);
  const sessionLabel = cycleClosed
    ? `Ciclo completado`
    : `Sesión ${cycle.currentSession} de ${cycle.totalSessions}`;

  return (
    <section className="card overflow-hidden">
      <div className="h-1.5 bg-linear-to-r from-primary-500 via-primary-300 to-accent-500" />
      <div className="grid grid-cols-1 gap-6 p-5 md:grid-cols-2 xl:grid-cols-12 xl:gap-0 xl:divide-x xl:divide-slate-100">
        {/* ---------- Paciente ---------- */}
        <div className="md:col-span-2 xl:col-span-5 xl:pr-6">
          <p className="label">Paciente</p>
          <div className="flex items-start gap-4">
            <Avatar initials={patient.initials} size="xl" tone="soft" />
            <div className="min-w-0">
              <h1 className="text-xl font-bold leading-tight text-navy">{patient.fullName}</h1>
              <p className="mt-1 text-sm text-slate-500">
                {patient.historyNumber} · {age} años · {patient.sex === "M" ? "Masculino" : "Femenino"} · Nac.{" "}
                {formatDate(patient.birthDate)}
              </p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {patient.diagnoses.map((d) => (
                  <Badge key={d} tone="navy">
                    {d}
                  </Badge>
                ))}
              </div>
              <div className="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-xs text-slate-600">
                <span className="inline-flex items-center gap-1.5">
                  <Phone size={13} className="shrink-0 text-primary-500" />
                  {patient.guardian.name} ({patient.guardian.relation}) · {patient.guardian.phone}
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <ShieldCheck size={13} className="shrink-0 text-primary-500" />
                  {patient.coverage}
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <GraduationCap size={13} className="shrink-0 text-primary-500" />
                  {patient.school}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* ---------- Profesional ---------- */}
        <div className="xl:col-span-3 xl:px-6">
          <p className="label">Profesional a cargo</p>
          <div className="flex items-start gap-3">
            <Avatar initials={professional.initials} size="md" tone="primary" />
            <div className="min-w-0">
              <p className="font-semibold leading-tight text-navy">{professional.fullName}</p>
              <p className="mt-1 inline-flex items-center gap-1.5 text-sm text-slate-600">
                <Stethoscope size={14} className="text-primary-500" />
                {specialty.name}
              </p>
              <p className="inline-flex items-center gap-1.5 text-sm text-slate-600">
                <IdCard size={14} className="text-primary-500" />
                {professional.college} {professional.licenseNumber}
              </p>
            </div>
          </div>
          <div className="mt-4 rounded-lg bg-surface px-3 py-2 text-xs leading-relaxed text-slate-600">
            <span className="font-semibold text-navy">Ciclo</span> {cycle.id}
            <br />
            Inicio {formatDate(cycle.startDate)} · {cycle.frequency}
          </div>
        </div>

        {/* ---------- Sesión actual ---------- */}
        <div className="flex flex-col xl:col-span-4 xl:pl-6">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="label">Sesión actual</p>
              <p className="text-2xl font-extrabold leading-none text-navy">{sessionLabel}</p>
              <p className="mt-1.5 inline-flex items-center gap-1.5 text-xs text-slate-500">
                <CalendarCheck size={13} className="text-primary-500" />
                {cycleClosed
                  ? `${cycle.totalSessions} sesiones registradas · ciclo de 12 semanas cerrado`
                  : `Semana ${cycle.currentSession} de ${cycle.totalSessions} · ${formatDate(draft.date)}`}
              </p>
            </div>
            <Badge tone={cycleClosed ? "success" : "accent"} dot>
              {cycleClosed ? "Cerrado" : "En curso"}
            </Badge>
          </div>
          <div className="mt-3">
            <SessionStepper total={cycle.totalSessions} current={cycle.currentSession} closed={cycleClosed} />
          </div>
          <div className="no-print mt-auto grid gap-2 pt-4 sm:grid-cols-2 xl:grid-cols-1 2xl:grid-cols-2">
            <Button variant="accent" icon={FileDown} onClick={onGeneratePdf} className="whitespace-nowrap">
              Generar Reporte PDF
            </Button>
            {cycleClosed ? (
              /* El ciclo ya no admite sesiones: se ofrece abrir el siguiente (formulario en el historial). */
              <Button
                variant="primary"
                icon={CalendarPlus}
                onClick={onOpenNextCycle}
                disabled={!onOpenNextCycle}
                className="whitespace-nowrap"
              >
                Abrir siguiente ciclo
              </Button>
            ) : (
              <Button variant="primary" icon={Save} onClick={onSave} className="whitespace-nowrap">
                Guardar sesión {cycle.currentSession}
              </Button>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
