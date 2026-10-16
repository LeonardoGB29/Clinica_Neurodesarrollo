const sizes = {
  sm: "h-8 w-8 text-xs",
  md: "h-10 w-10 text-sm",
  lg: "h-14 w-14 text-lg",
  xl: "h-16 w-16 text-xl",
};

const tones = {
  primary: "bg-primary-500 text-white",
  navy: "bg-navy text-white",
  accent: "bg-accent-500 text-white",
  soft: "bg-primary-50 text-primary-700 ring-2 ring-primary-100",
};

export default function Avatar({ initials, size = "md", tone = "primary", className = "" }) {
  return (
    <div
      className={`grid shrink-0 place-items-center rounded-full font-bold ${sizes[size] ?? sizes.md} ${tones[tone] ?? tones.primary} ${className}`}
      aria-hidden="true"
    >
      {initials}
    </div>
  );
}
