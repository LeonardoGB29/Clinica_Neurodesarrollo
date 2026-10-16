import { Lock } from "lucide-react";
import { GAS_LEVELS } from "../../utils/progress";

const fill = {
  "-2": "bg-rose-500 text-white",
  "-1": "bg-amber-400 text-white",
  0: "bg-primary-500 text-white",
  1: "bg-emerald-500 text-white",
  2: "bg-emerald-700 text-white",
};

const outline = {
  "-2": "border-rose-400 text-rose-600",
  "-1": "border-amber-400 text-amber-600",
  0: "border-primary-400 text-primary-600",
  1: "border-emerald-500 text-emerald-600",
  2: "border-emerald-700 text-emerald-800",
};

/**
 * Indicador de la escala GAS (-2 … +2).
 *  - locked: el GAS definitivo se habilita al cierre del ciclo (sesión 12);
 *    mientras tanto se muestra la proyección con borde punteado.
 */
export default function GasScale({ level, projected, locked }) {
  const active = locked ? projected : level;

  return (
    <div>
      <div className="flex items-center justify-between gap-2">
        <span className="text-[11px] font-semibold uppercase tracking-wide text-navy-500">Escala GAS</span>
        {locked ? (
          <span className="inline-flex items-center gap-1 text-[11px] text-slate-500">
            <Lock size={11} /> Se calcula al cierre (S12)
          </span>
        ) : (
          <span className="text-[11px] font-medium text-emerald-700">Habilitada · cierre de ciclo</span>
        )}
      </div>

      <div className="mt-1.5 grid grid-cols-5 gap-1" role="img" aria-label={active ? `Nivel GAS ${active.level}` : "GAS sin datos"}>
        {GAS_LEVELS.map((g) => {
          const key = String(g.level);
          const isActive = active?.level === g.level;
          const cls = !isActive
            ? "bg-slate-100 text-slate-400"
            : locked
              ? `border-2 border-dashed bg-white ${outline[key]}`
              : fill[key];
          return (
            <div
              key={g.level}
              title={`${g.level > 0 ? "+" : ""}${g.level} · ${g.label} → ${g.decision}`}
              className={`h-7 rounded-md text-center text-xs font-bold leading-6 transition ${cls}`}
            >
              {g.level > 0 ? `+${g.level}` : g.level}
            </div>
          );
        })}
      </div>

      <p className="mt-1.5 text-xs text-slate-600">
        {active ? (
          <>
            {locked ? "Proyección: " : "Resultado: "}
            <span className="font-semibold text-navy">{active.label}</span> · {active.decision}
          </>
        ) : (
          "Sin sesiones evaluadas todavía."
        )}
      </p>
    </div>
  );
}
