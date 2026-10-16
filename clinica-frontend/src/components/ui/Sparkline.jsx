/**
 * Mini-gráfico de tendencia (SVG) para la Vista 360°.
 */
export default function Sparkline({ data = [], width = 72, height = 24, color = "#0083A8", max = 100 }) {
  if (!data.length) return null;
  const pad = 3;
  const stepX = data.length > 1 ? (width - pad * 2) / (data.length - 1) : 0;
  const pts = data.map((v, i) => [
    pad + i * stepX,
    height - pad - (Math.max(0, Math.min(max, v)) / max) * (height - pad * 2),
  ]);
  const d = pts.map(([x, y], i) => `${i === 0 ? "M" : "L"}${x.toFixed(1)},${y.toFixed(1)}`).join(" ");
  const [lx, ly] = pts[pts.length - 1];

  return (
    <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`} aria-hidden="true" className="shrink-0">
      <path d={d} fill="none" stroke={color} strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />
      <circle cx={lx} cy={ly} r="3.5" fill={color} stroke="#fff" strokeWidth="2" />
    </svg>
  );
}
