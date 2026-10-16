const variants = {
  primary: "bg-primary-500 text-white shadow-sm hover:bg-primary-600 focus-visible:ring-primary-100",
  accent: "bg-accent-500 text-white shadow-sm hover:bg-accent-600 focus-visible:ring-accent-100",
  outline: "border border-slate-200 bg-white text-navy hover:bg-slate-50 focus-visible:ring-primary-100",
  ghost: "bg-transparent text-slate-600 hover:bg-slate-100 hover:text-navy focus-visible:ring-primary-100",
  danger: "border border-rose-200 bg-white text-rose-600 hover:bg-rose-50 focus-visible:ring-rose-100",
};

const sizes = {
  sm: "h-8 gap-1.5 px-3 text-xs",
  md: "h-10 gap-2 px-4 text-sm",
  lg: "h-11 gap-2 px-5 text-sm",
  icon: "h-10 w-10 p-0",
};

export default function Button({
  variant = "primary",
  size = "md",
  icon: Icon,
  children,
  className = "",
  type = "button",
  ...rest
}) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center rounded-lg font-semibold transition focus:outline-none focus-visible:ring-4 disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant] ?? variants.primary} ${sizes[size] ?? sizes.md} ${className}`}
      {...rest}
    >
      {Icon && <Icon size={size === "sm" ? 14 : 16} />}
      {children}
    </button>
  );
}
