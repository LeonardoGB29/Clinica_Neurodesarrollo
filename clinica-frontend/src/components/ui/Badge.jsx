const styles = {
  primary: "bg-primary-50 text-primary-700 ring-primary-100",
  accent: "bg-accent-50 text-accent-700 ring-accent-100",
  success: "bg-emerald-50 text-emerald-700 ring-emerald-100",
  warning: "bg-amber-50 text-amber-700 ring-amber-100",
  danger: "bg-rose-50 text-rose-700 ring-rose-100",
  neutral: "bg-slate-100 text-slate-600 ring-slate-200",
  navy: "bg-navy-50 text-navy-700 ring-navy-100",
};

export default function Badge({ tone = "neutral", dot = false, children, className = "", style, title }) {
  return (
    <span
      title={title}
      style={style}
      className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${styles[tone] ?? styles.neutral} ${className}`}
    >
      {dot && <span className="h-1.5 w-1.5 rounded-full bg-current" />}
      {children}
    </span>
  );
}
