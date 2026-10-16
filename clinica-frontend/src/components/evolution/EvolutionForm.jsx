import { Activity, Award, CalendarDays, CircleAlert, Eraser, NotebookPen, Save } from "lucide-react";
import Badge from "../ui/Badge";
import Button from "../ui/Button";
import ProgressBar from "../ui/ProgressBar";
import { successRate } from "../../utils/progress";
import { seriesColor } from "../../utils/theme";

function TextField({ icon: Icon, id, label, value, onChange, error, placeholder, required }) {
  return (
    <div>
      <div className="flex items-center justify-between gap-2">
        <label htmlFor={id} className="label mb-0 inline-flex items-center gap-1.5">
          <Icon size={12} className="text-primary-500" />
          {label}
          {required && <span className="text-accent-600">*</span>}
        </label>
        <span className="text-[11px] text-slate-400">{value.length} caracteres</span>
      </div>
      <textarea
        id={id}
        rows={3}
        className={`input mt-1.5 resize-y ${error ? "border-rose-300 focus:border-rose-400 focus:ring-rose-100" : ""}`}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
      />
      {error && (
        <p className="mt-1 inline-flex items-center gap-1 text-xs text-rose-600">
          <CircleAlert size={12} />
          {error}
        </p>
      )}
    </div>
  );
}

/**
 * Formulario estándar de evolución (igual para Ocupacional, Lenguaje, Física y Psicología).
 */
export default function EvolutionForm({
  draft,
  objectives,
  errors,
  showErrors,
  totalSessions,
  onChange,
  onEvalChange,
  onSave,
  onReset,
}) {
  const err = (key) => (showErrors && errors[key] ? errors[key] : null);
  const isLast = draft.number >= totalSessions;

  return (
    <li className="relative pl-11" id="evolution-form">
      <span className="absolute left-0 top-1 grid h-8 w-8 place-items-center rounded-full bg-accent-500 text-xs font-bold text-white ring-4 ring-white">
        <span className="absolute inset-0 animate-ping rounded-full bg-accent-400 opacity-40" />
        <span className="relative">{draft.number}</span>
      </span>

      <form
        className="rounded-xl border-2 border-primary-200 bg-white shadow-card"
        onSubmit={(e) => {
          e.preventDefault();
          onSave();
        }}
        noValidate
      >
        <div className="flex flex-wrap items-center gap-3 border-b border-slate-100 px-4 py-3">
          <div className="min-w-0 flex-1">
            <p className="flex flex-wrap items-center gap-2 font-semibold text-navy">
              Sesión {draft.number}
              <Badge tone="accent" dot>
                En curso
              </Badge>
            </p>
            <p className="text-xs text-slate-500">
              Formulario estándar de evolución · Ocupacional · Lenguaje · Física · Psicología
            </p>
          </div>
          <label className="inline-flex items-center gap-2 text-xs text-slate-600">
            <CalendarDays size={14} className="text-primary-500" />
            Fecha
            <input
              type="date"
              className="input h-8 w-auto py-0 text-xs"
              value={draft.date}
              onChange={(e) => onChange({ date: e.target.value })}
            />
          </label>
        </div>

        <div className="space-y-5 px-4 py-4">
          <section>
            <p className="label">Evaluación de objetivos activos (aciertos / intentos → % de la sesión)</p>
            {objectives.length === 0 && (
              <p className="text-sm text-slate-500">Agrega al menos un objetivo para evaluarlo en esta sesión.</p>
            )}
            <ul className="space-y-3">
              {objectives.map((o, i) => {
                const ev = draft.evaluations[o.id] ?? { hits: "", total: "10" };
                const v = successRate(ev);
                const color = seriesColor(i);
                const error = err(o.id);
                return (
                  <li
                    key={o.id}
                    className={`rounded-lg border p-3 ${error ? "border-rose-300 bg-rose-50/40" : "border-slate-200"}`}
                  >
                    <div className="flex flex-wrap items-center gap-3">
                      <span
                        className="grid h-6 w-6 shrink-0 place-items-center rounded-full text-[11px] font-bold text-white"
                        style={{ backgroundColor: color }}
                      >
                        {i + 1}
                      </span>
                      <p className="min-w-[180px] flex-1 text-sm font-medium text-navy">{o.text}</p>
                      <div className="flex items-center gap-2 text-sm text-slate-600">
                        <label className="sr-only" htmlFor={`hits-${o.id}`}>
                          Aciertos
                        </label>
                        <input
                          id={`hits-${o.id}`}
                          type="number"
                          min={0}
                          inputMode="numeric"
                          className="input h-9 w-20 text-center"
                          placeholder="0"
                          value={ev.hits}
                          onChange={(e) => onEvalChange(o.id, { hits: e.target.value })}
                        />
                        <span>de</span>
                        <label className="sr-only" htmlFor={`total-${o.id}`}>
                          Intentos
                        </label>
                        <input
                          id={`total-${o.id}`}
                          type="number"
                          min={1}
                          inputMode="numeric"
                          className="input h-9 w-20 text-center"
                          value={ev.total}
                          onChange={(e) => onEvalChange(o.id, { total: e.target.value })}
                        />
                        <span className="text-xs text-slate-500">intentos</span>
                        <span
                          className={`ml-1 min-w-[56px] rounded-full px-2.5 py-1 text-center text-sm font-bold ${
                            v === null ? "bg-slate-100 text-slate-400" : "bg-primary-50 text-primary-700"
                          }`}
                        >
                          {v === null ? "— %" : `${v}%`}
                        </span>
                      </div>
                    </div>
                    <ProgressBar value={v ?? 0} color={color} size="sm" className="mt-2" />
                    {error && (
                      <p className="mt-1.5 inline-flex items-center gap-1 text-xs text-rose-600">
                        <CircleAlert size={12} />
                        {error}
                      </p>
                    )}
                  </li>
                );
              })}
            </ul>
          </section>

          <TextField
            icon={Activity}
            id="activity"
            label="Actividad realizada / Evolución"
            required
            value={draft.activity}
            onChange={(v) => onChange({ activity: v })}
            error={err("activity")}
            placeholder="Describe las actividades realizadas, materiales, apoyos brindados y la respuesta del paciente…"
          />
          <TextField
            icon={Award}
            id="achievement"
            label="Logro"
            required
            value={draft.achievement}
            onChange={(v) => onChange({ achievement: v })}
            error={err("achievement")}
            placeholder="¿Qué logró el paciente en esta sesión respecto a los objetivos?"
          />
          <TextField
            icon={NotebookPen}
            id="observations"
            label="Observaciones y recomendaciones"
            value={draft.observations}
            onChange={(v) => onChange({ observations: v })}
            placeholder="Recomendaciones para casa, colegio u otras especialidades…"
          />
        </div>

        <div className="no-print flex flex-wrap items-center justify-between gap-3 rounded-b-xl border-t border-slate-100 bg-surface/70 px-4 py-3">
          <p className="text-xs text-slate-500">
            Al guardar, la sesión {draft.number} pasa a solo lectura y{" "}
            {isLast ? "se cierra el ciclo de 12 semanas." : `se habilita la sesión ${draft.number + 1}.`}
          </p>
          <div className="flex gap-2">
            <Button variant="outline" icon={Eraser} onClick={onReset}>
              Limpiar
            </Button>
            <Button type="submit" variant="primary" icon={Save}>
              Guardar sesión {draft.number}
            </Button>
          </div>
        </div>
      </form>
    </li>
  );
}
