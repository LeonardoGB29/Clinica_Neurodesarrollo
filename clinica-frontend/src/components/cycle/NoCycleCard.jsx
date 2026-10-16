import { CircleAlert, IdCard, Stethoscope } from "lucide-react";
import Avatar from "../ui/Avatar";
import Badge from "../ui/Badge";
import NewCycleForm, { DEFAULT_TOTAL_SESSIONS } from "./NewCycleForm";
import { ageFromBirthDate, formatDate } from "../../utils/progress";

/**
 * Tarjeta que reemplaza a la ficha cuando GET /pacientes/{id}/ciclos responde 404: el paciente
 * no tiene un ciclo abierto de la especialidad del profesional (p. ej. tras cerrar el anterior
 * en la sesión 12). Permite abrir uno nuevo con POST /pacientes/{id}/ciclos.
 */
export default function NoCycleCard({ patient, professional, specialty, onOpen, busy }) {
  const age = ageFromBirthDate(patient.birthDate);

  return (
    <section className="card overflow-hidden" aria-labelledby="sin-ciclo-titulo">
      <div className="h-1.5 bg-linear-to-r from-primary-500 via-primary-300 to-accent-500" />
      <div className="grid grid-cols-1 gap-6 p-5 lg:grid-cols-5 lg:gap-0 lg:divide-x lg:divide-slate-100">
        {/* ---------- Paciente y profesional ---------- */}
        <div className="lg:col-span-2 lg:pr-6">
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
            </div>
          </div>

          <p className="label mt-5">Profesional a cargo</p>
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
        </div>

        {/* ---------- Sin ciclo abierto → abrir uno nuevo ---------- */}
        <div className="lg:col-span-3 lg:pl-6">
          <div className="flex items-start gap-3">
            <span className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-accent-50 text-accent-600">
              <CircleAlert size={20} />
            </span>
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <h2 id="sin-ciclo-titulo" className="text-lg font-bold text-navy">
                  Sin ciclo abierto de {specialty.name}
                </h2>
                <Badge tone="warning" dot>
                  Sin ciclo
                </Badge>
              </div>
              <p className="mt-1 text-sm leading-relaxed text-slate-600">
                El paciente no tiene un ciclo terapéutico abierto de {specialty.short}: el anterior quedó cerrado o
                aún no se ha iniciado uno. Abre un ciclo nuevo para habilitar la ficha de evolución (sesión 1 de{" "}
                {DEFAULT_TOTAL_SESSIONS}), la gestión de objetivos y el dashboard de progreso.
              </p>
            </div>
          </div>

          <div className="mt-5 rounded-xl border border-slate-200 bg-surface/60 p-4">
            <NewCycleForm specialty={specialty} onOpen={onOpen} busy={busy} />
          </div>
        </div>
      </div>
    </section>
  );
}
