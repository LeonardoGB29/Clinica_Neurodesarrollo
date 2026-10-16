/**
 * Tokens compartidos para gráficos y componentes de progreso.
 * Los colores de serie se validaron (contraste, daltonismo) antes de fijarlos.
 */

/** Colores de serie para los objetivos del ciclo (índice 0, 1, 2). */
export const SERIES_COLORS = ["#0083A8", "#D9780A", "#6F4FB3"];

export const seriesColor = (index) => SERIES_COLORS[index % SERIES_COLORS.length];

/** Tonos semánticos para barras y anillos (clases Tailwind completas para que el compilador las detecte). */
export const TONES = {
  primary: { bar: "bg-primary-500", track: "bg-primary-100", text: "text-primary-700", hex: "#0083A8" },
  success: { bar: "bg-emerald-500", track: "bg-emerald-100", text: "text-emerald-700", hex: "#2E9E6B" },
  warning: { bar: "bg-accent-500", track: "bg-accent-100", text: "text-accent-700", hex: "#FF9E1B" },
  danger: { bar: "bg-rose-500", track: "bg-rose-100", text: "text-rose-700", hex: "#E11D48" },
  neutral: { bar: "bg-slate-400", track: "bg-slate-100", text: "text-slate-600", hex: "#94A3B8" },
  muted: { bar: "bg-slate-300", track: "bg-slate-100", text: "text-slate-500", hex: "#CBD5E1" },
};
