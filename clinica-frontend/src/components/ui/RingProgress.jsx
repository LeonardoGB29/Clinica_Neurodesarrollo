/**
 * Anillo de progreso en SVG puro (sin dependencias) para el % acumulado.
 */
export default function RingProgress({
  value = 0,
  size = 96,
  stroke = 9,
  color = "#0083A8",
  label,
  children,
}) {
  const v = Math.max(0, Math.min(100, Number(value) || 0));
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const offset = c - (v / 100) * c;

  return (
    <div className="relative inline-grid shrink-0 place-items-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="-rotate-90" aria-hidden="true">
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#E9EEF2" strokeWidth={stroke} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={c}
          strokeDashoffset={offset}
          className="transition-[stroke-dashoffset] duration-700 ease-out"
        />
      </svg>
      <div className="absolute inset-0 grid place-items-center text-center">
        {children ?? (
          <div>
            <div className="text-xl font-bold leading-none text-navy">{v}%</div>
            {label && <div className="mt-1 text-[10px] uppercase tracking-wide text-slate-500">{label}</div>}
          </div>
        )}
      </div>
    </div>
  );
}
