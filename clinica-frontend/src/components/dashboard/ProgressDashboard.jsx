import { useState } from "react";
import { ChartLine, Gauge, Minus, TrendingDown, TrendingUp } from "lucide-react";
import Card from "../ui/Card";
import RingProgress from "../ui/RingProgress";
import CycleChart from "./CycleChart";
import GasScale from "./GasScale";
import {
  accumulatedProgress,
  computeGas,
  isCycleClosing,
  lastDelta,
  lastScore,
  recordedCount,
  scoreSeries,
} from "../../utils/progress";
import { seriesColor } from "../../utils/theme";

function DeltaBadge({ delta }) {
  if (delta === null || delta === undefined) return null;
  const Icon = delta > 0 ? TrendingUp : delta < 0 ? TrendingDown : Minus;
  const cls =
    delta > 0
      ? "bg-emerald-50 text-emerald-700"
      : delta < 0
        ? "bg-rose-50 text-rose-700"
        : "bg-slate-100 text-slate-600";
  return (
    <span
      className={`inline-flex items-center gap-0.5 rounded-full px-1.5 py-0.5 text-[11px] font-semibold ${cls}`}
      title="Variación respecto a la sesión anterior"
    >
      <Icon size={11} />
      {delta > 0 ? "+" : ""}
      {delta} pts
    </span>
  );
}

/** Mini-columnas con la tasa de éxito de cada sesión del ciclo (S1 … S12). */
function SessionBars({ series, totalSessions, currentSession, color }) {
  return (
    <div className="flex h-12 items-end gap-1">
      {Array.from({ length: totalSessions }, (_, i) => {
        const n = i + 1;
        const v = series.find((s) => s.session === n)?.value ?? null;
        const isCurrent = n === currentSession;
        return (
          <div
            key={n}
            className="flex flex-1 items-end"
            title={v !== null ? `Sesión ${n}: ${v}%` : `Sesión ${n}: ${isCurrent ? "en curso" : "sin registro"}`}
          >
            <div
              className={`w-full rounded-t-[3px] ${
                v === null ? (isCurrent ? "border border-dashed border-accent-400 bg-accent-50" : "bg-slate-100") : ""
              }`}
              style={{ height: v === null ? 8 : Math.max(4, Math.round(v * 0.44)), backgroundColor: v === null ? undefined : color }}
            />
          </div>
        );
      })}
    </div>
  );
}

function ObjectiveProgressCard({ objective, index, sessions, cycle, gasEnabled }) {
  const color = seriesColor(index);
  const accumulated = accumulatedProgress(sessions, objective.id);
  const last = lastScore(sessions, objective.id);
  const delta = lastDelta(sessions, objective.id);
  const count = recordedCount(sessions, objective.id);
  const gas = computeGas(last, objective.baseline, objective.target);
  const series = scoreSeries(sessions, objective.id);

  return (
    <article className="flex flex-col rounded-xl border border-slate-200 bg-white p-4">
      <header className="flex items-start gap-2">
        <span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full" style={{ backgroundColor: color }} />
        <h3 className="line-clamp-2 text-sm font-semibold leading-snug text-navy" title={objective.text}>
          {objective.text}
        </h3>
      </header>

      <div className="mt-4 flex items-center gap-4">
        <RingProgress value={accumulated} color={color} label="acumulado" />
        <dl className="grid flex-1 grid-cols-2 gap-x-3 gap-y-2 text-xs">
          <div>
            <dt className="text-slate-500">Última sesión</dt>
            <dd className="flex flex-wrap items-center gap-1.5 text-base font-bold text-navy">
              {last === null ? "—" : `${last}%`}
              <DeltaBadge delta={delta} />
            </dd>
          </div>
          <div>
            <dt className="text-slate-500">Meta del ciclo</dt>
            <dd className="text-base font-bold text-navy">{objective.target}%</dd>
          </div>
          <div>
            <dt className="text-slate-500">Línea base</dt>
            <dd className="font-semibold text-slate-700">{objective.baseline}%</dd>
          </div>
          <div>
            <dt className="text-slate-500">Sesiones evaluadas</dt>
            <dd className="font-semibold text-slate-700">
              {count} de {cycle.totalSessions}
            </dd>
          </div>
        </dl>
      </div>

      <div className="mt-4">
        <div className="flex items-center justify-between text-[11px] text-slate-500">
          <span>Tasa de éxito por sesión</span>
          <span>S1 → S{cycle.totalSessions}</span>
        </div>
        <SessionBars
          series={series}
          totalSessions={cycle.totalSessions}
          currentSession={cycle.status === "closed" ? null : cycle.currentSession}
          color={color}
        />
      </div>

      <div className="mt-4 border-t border-slate-100 pt-3">
        <GasScale level={gas} projected={gas} locked={!gasEnabled} />
      </div>
    </article>
  );
}

function Switch({ checked, onChange, label }) {
  return (
    <label className="no-print inline-flex cursor-pointer select-none items-center gap-2 text-xs font-medium text-slate-600">
      <span>{label}</span>
      <span className={`relative inline-flex h-5 w-9 items-center rounded-full transition ${checked ? "bg-primary-500" : "bg-slate-300"}`}>
        <input type="checkbox" className="peer sr-only" checked={checked} onChange={(e) => onChange(e.target.checked)} />
        <span className={`absolute h-4 w-4 rounded-full bg-white shadow transition-all ${checked ? "left-[18px]" : "left-0.5"}`} />
      </span>
    </label>
  );
}

export default function ProgressDashboard({ objectives, sessions, cycle }) {
  const [simulateClose, setSimulateClose] = useState(false);
  const closing = isCycleClosing(cycle);
  const gasEnabled = closing || simulateClose;

  return (
    <Card
      id="dashboard"
      title="Dashboard de progreso"
      subtitle="% de avance acumulado por objetivo (promedio de la tasa de éxito en las sesiones registradas) y escala GAS al cierre del ciclo"
      icon={Gauge}
      actions={
        !closing && <Switch checked={simulateClose} onChange={setSimulateClose} label="Simular cierre de ciclo (GAS)" />
      }
    >
      {objectives.length ? (
        <>
          {gasEnabled && (
            <div className="mb-4 rounded-lg border border-emerald-100 bg-emerald-50 px-3 py-2 text-xs text-emerald-800">
              Escala GAS habilitada:{" "}
              {closing
                ? "el ciclo de 12 semanas está en su sesión final. Decide si se sube, mantiene o baja la carga de cada objetivo."
                : "vista simulada del cierre de ciclo con los datos registrados hasta hoy."}
            </div>
          )}

          <div className="grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
            {objectives.map((o, i) => (
              <ObjectiveProgressCard
                key={o.id}
                objective={o}
                index={i}
                sessions={sessions}
                cycle={cycle}
                gasEnabled={gasEnabled}
              />
            ))}
          </div>

          <div className="mt-6">
            <div className="mb-2 flex items-center gap-2">
              <ChartLine size={16} className="text-primary-500" />
              <h3 className="text-sm font-semibold text-navy">Evolución por sesión</h3>
            </div>
            <CycleChart objectives={objectives} sessions={sessions} cycle={cycle} />
          </div>
        </>
      ) : (
        <div className="rounded-xl border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500">
          Agrega objetivos al ciclo para visualizar su progreso.
        </div>
      )}
    </Card>
  );
}
