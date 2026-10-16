import { MessageSquareText, Orbit, Quote, Target } from "lucide-react";
import Badge from "../ui/Badge";
import Card from "../ui/Card";
import ProgressBar from "../ui/ProgressBar";
import Sparkline from "../ui/Sparkline";
import { formatDate, lastScore } from "../../utils/progress";

const average = (values) =>
  values.length ? Math.round(values.reduce((a, b) => a + b, 0) / values.length) : 0;

/**
 * Vista 360° interdisciplinaria (solo lectura): objetivos activos y % de
 * progreso del paciente en las demás especialidades, para correlacionar terapias.
 */
export default function View360({
  otherSpecialties,
  specialties,
  currentSpecialty,
  objectives,
  sessions,
  sharedNotes,
}) {
  const currentLevel = average(objectives.map((o) => lastScore(sessions, o.id)).filter((v) => v !== null));

  const summary = [
    { id: currentSpecialty.id, name: currentSpecialty.name, color: currentSpecialty.color, value: currentLevel, mine: true },
    ...otherSpecialties.map((s) => ({
      id: s.specialtyId,
      name: specialties[s.specialtyId].name,
      color: specialties[s.specialtyId].color,
      value: average(s.objectives.map((o) => o.history[o.history.length - 1])),
    })),
  ];

  return (
    <Card
      id="vista-360"
      title="Vista 360° interdisciplinaria"
      subtitle="Objetivos y progreso del paciente en las demás especialidades · solo lectura"
      icon={Orbit}
      bodyClassName="space-y-5"
    >
      {/* Resumen por especialidad */}
      <section>
        <p className="label">Nivel actual promedio por especialidad</p>
        <ul className="space-y-2">
          {summary.map((s) => (
            <li key={s.id} className="grid grid-cols-[132px_1fr_auto] items-center gap-2 text-xs">
              <span className="inline-flex min-w-0 items-center gap-1.5">
                <span className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ backgroundColor: s.color }} />
                <span className="truncate font-medium text-navy" title={s.name}>
                  {s.name}
                </span>
                {s.mine && <span className="shrink-0 text-[10px] text-slate-400">(tú)</span>}
              </span>
              <ProgressBar value={s.value} color={s.color} size="sm" />
              <span className="w-9 text-right font-semibold tabular-nums text-slate-700">{s.value}%</span>
            </li>
          ))}
        </ul>
      </section>

      {/* Detalle por especialidad */}
      {otherSpecialties.map((sp) => {
        const meta = specialties[sp.specialtyId];
        return (
          <section
            key={sp.specialtyId}
            className="rounded-xl border border-slate-200 p-3.5"
            style={{ borderLeftWidth: 4, borderLeftColor: meta.color }}
          >
            <header className="flex flex-wrap items-start justify-between gap-2">
              <div className="min-w-0">
                <h3 className="text-sm font-semibold text-navy">{meta.name}</h3>
                <p className="text-xs text-slate-500">
                  {sp.professionalName} · {sp.license}
                </p>
              </div>
              <div className="text-right">
                <Badge tone="neutral">
                  Sesión {sp.currentSession} de {sp.totalSessions}
                </Badge>
                <p className="mt-1 text-[11px] text-slate-400">Última: {formatDate(sp.lastSessionDate)}</p>
              </div>
            </header>

            <ul className="mt-3 space-y-3">
              {sp.objectives.map((o) => {
                const last = o.history[o.history.length - 1];
                return (
                  <li key={o.id}>
                    <p className="text-xs font-medium leading-snug text-slate-700">{o.text}</p>
                    <div className="mt-1.5 flex items-center gap-2">
                      <ProgressBar value={last} color={meta.color} size="sm" className="flex-1" />
                      <span className="w-9 text-right text-xs font-bold tabular-nums text-navy">{last}%</span>
                      <Sparkline data={o.history} color={meta.color} width={64} height={22} />
                    </div>
                    <p className="mt-0.5 inline-flex items-center gap-1 text-[11px] text-slate-400">
                      <Target size={11} /> Meta {o.target}% · {o.history.length} sesiones evaluadas
                    </p>
                  </li>
                );
              })}
            </ul>

            <p className="mt-3 flex gap-2 rounded-lg bg-surface px-3 py-2 text-xs italic leading-relaxed text-slate-600">
              <Quote size={12} className="mt-0.5 shrink-0 text-primary-400" />
              {sp.lastNote}
            </p>
          </section>
        );
      })}

      {/* Notas compartidas */}
      <section>
        <p className="label inline-flex items-center gap-1.5">
          <MessageSquareText size={12} className="text-primary-500" />
          Notas compartidas del equipo
        </p>
        <ul className="space-y-2">
          {sharedNotes.map((n) => {
            const meta = specialties[n.specialtyId];
            return (
              <li key={n.id} className="rounded-lg border border-slate-200 px-3 py-2 text-xs">
                <div className="flex items-center justify-between gap-2">
                  <span className="inline-flex items-center gap-1.5 font-semibold text-navy">
                    <span className="h-2 w-2 rounded-full" style={{ backgroundColor: meta.color }} />
                    {meta.short}
                  </span>
                  <span className="text-slate-400">{formatDate(n.date)}</span>
                </div>
                <p className="mt-1 leading-relaxed text-slate-600">{n.text}</p>
              </li>
            );
          })}
        </ul>
      </section>
    </Card>
  );
}
