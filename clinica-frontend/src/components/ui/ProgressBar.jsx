import { TONES } from "../../utils/theme";

/**
 * Barra de progreso. Acepta un tono semántico (primary/success/...) o un color hex
 * explícito (usado para colorear por objetivo o por especialidad).
 */
export default function ProgressBar({
  value = 0,
  tone = "primary",
  size = "md",
  color,
  showValue = false,
  className = "",
}) {
  const v = Math.max(0, Math.min(100, Number(value) || 0));
  const h = size === "sm" ? "h-1.5" : size === "lg" ? "h-3" : "h-2";
  const t = TONES[tone] ?? TONES.primary;
  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <div
        className={`flex-1 ${h} overflow-hidden rounded-full ${color ? "bg-slate-100" : t.track}`}
        role="progressbar"
        aria-valuenow={v}
        aria-valuemin={0}
        aria-valuemax={100}
      >
        <div
          className={`${h} rounded-full transition-all duration-500 ${color ? "" : t.bar}`}
          style={{ width: `${v}%`, backgroundColor: color }}
        />
      </div>
      {showValue && (
        <span className="w-10 text-right text-xs font-semibold tabular-nums text-slate-600">{v}%</span>
      )}
    </div>
  );
}
