import { Activity, Award, ChevronDown, Lock, NotebookPen } from "lucide-react";
import Badge from "../ui/Badge";
import ProgressBar from "../ui/ProgressBar";
import { formatDate, successRate } from "../../utils/progress";
import { seriesColor } from "../../utils/theme";

function Field({ icon: Icon, label, text }) {
  return (
    <div>
      <p className="label inline-flex items-center gap-1.5">
        <Icon size={12} className="text-primary-500" />
        {label}
      </p>
      <p className="whitespace-pre-line text-sm leading-relaxed text-slate-700">
        {text || <span className="text-slate-400">Sin registro</span>}
      </p>
    </div>
  );
}

/** Registro de una sesión ya guardada (solo lectura) dentro de la ficha en cascada. */
export default function SessionRecord({ session, objectives, expanded, onToggle }) {
  return (
    <li className="relative pl-11">
      <span className="absolute left-0 top-1 grid h-8 w-8 place-items-center rounded-full bg-primary-500 text-xs font-bold text-white ring-4 ring-white">
        {session.number}
      </span>

      <div className="rounded-xl border border-slate-200 bg-slate-50/70">
        <button
          type="button"
          onClick={onToggle}
          aria-expanded={expanded}
          className="flex w-full items-center gap-3 px-4 py-3 text-left"
        >
          <div className="min-w-0 flex-1">
            <p className="font-semibold text-navy">
              Sesión {session.number}{" "}
              <span className="font-normal text-slate-500">· {formatDate(session.date)}</span>
            </p>
            <p className="truncate text-xs text-slate-500">{session.professionalName}</p>
          </div>

          {!expanded && (
            <div className="hidden items-center gap-1.5 sm:flex">
              {objectives.map((o, i) => {
                const v = successRate(session.evaluations?.[o.id]);
                return (
                  <span
                    key={o.id}
                    className="inline-flex items-center gap-1 rounded-full bg-white px-2 py-0.5 text-[11px] font-semibold text-slate-700 ring-1 ring-slate-200"
                    title={o.text}
                  >
                    <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: seriesColor(i) }} />
                    {v === null ? "—" : `${v}%`}
                  </span>
                );
              })}
            </div>
          )}

          <Badge tone="neutral">
            <Lock size={11} /> Solo lectura
          </Badge>
          <ChevronDown size={16} className={`shrink-0 text-slate-400 transition ${expanded ? "rotate-180" : ""}`} />
        </button>

        {expanded && (
          <div className="space-y-4 border-t border-slate-200 px-4 py-4">
            <div>
              <p className="label">Evaluación de objetivos</p>
              <ul className="space-y-2">
                {objectives.map((o, i) => {
                  const ev = session.evaluations?.[o.id];
                  const v = successRate(ev);
                  return (
                    <li key={o.id} className="grid grid-cols-[auto_1fr_auto] items-center gap-3 text-sm">
                      <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: seriesColor(i) }} />
                      <div className="min-w-0">
                        <p className="truncate text-slate-700" title={o.text}>
                          {o.text}
                        </p>
                        {v !== null && <ProgressBar value={v} color={seriesColor(i)} size="sm" className="mt-1" />}
                      </div>
                      <span className="text-right text-xs tabular-nums">
                        {v === null ? (
                          <span className="text-slate-400">No evaluado (desde S{o.addedInSession})</span>
                        ) : (
                          <>
                            <span className="font-bold text-navy">{v}%</span>
                            <span className="block text-slate-500">
                              {ev.hits} de {ev.total}
                            </span>
                          </>
                        )}
                      </span>
                    </li>
                  );
                })}
              </ul>
            </div>
            <Field icon={Activity} label="Actividad realizada / Evolución" text={session.activity} />
            <Field icon={Award} label="Logro" text={session.achievement} />
            <Field icon={NotebookPen} label="Observaciones y recomendaciones" text={session.observations} />
          </div>
        )}
      </div>
    </li>
  );
}
