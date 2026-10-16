import {
  CartesianGrid,
  LabelList,
  Line,
  LineChart,
  ReferenceDot,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatDate, successRate } from "../../utils/progress";
import { seriesColor } from "../../utils/theme";

function ChartTooltip({ active, payload, label, objectives, sessions }) {
  if (!active || !payload?.length) return null;
  const n = Number(String(label).replace("S", ""));
  const session = sessions.find((s) => s.number === n);
  return (
    <div className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs shadow-card">
      <p className="font-semibold text-navy">
        Sesión {n}
        {session ? ` · ${formatDate(session.date)}` : " · sin registro"}
      </p>
      <ul className="mt-1 space-y-0.5">
        {payload.map((p) => {
          const i = objectives.findIndex((o) => o.id === p.dataKey);
          return (
            <li key={p.dataKey} className="flex items-center gap-2">
              <span className="h-2 w-2 rounded-full" style={{ backgroundColor: seriesColor(i) }} />
              <span className="text-slate-600">Objetivo {i + 1}</span>
              <span className="ml-auto pl-4 font-semibold tabular-nums text-navy">
                {p.value === null || p.value === undefined ? "—" : `${p.value}%`}
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

/**
 * Evolución de la tasa de éxito por sesión (1 línea por objetivo) sobre el
 * horizonte completo del ciclo (S1 … S12). Las metas se marcan en S12.
 */
export default function CycleChart({ objectives, sessions, cycle }) {
  const data = Array.from({ length: cycle.totalSessions }, (_, i) => {
    const n = i + 1;
    const s = sessions.find((x) => x.number === n);
    const row = { session: `S${n}`, n };
    objectives.forEach((o) => {
      row[o.id] = s ? successRate(s.evaluations?.[o.id]) : null;
    });
    return row;
  });

  const lastIndex = (objectiveId) => {
    let idx = -1;
    data.forEach((row, i) => {
      if (row[objectiveId] !== null && row[objectiveId] !== undefined) idx = i;
    });
    return idx;
  };

  return (
    <div>
      <ul className="mb-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-600">
        {objectives.map((o, i) => (
          <li key={o.id} className="inline-flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: seriesColor(i) }} />
            <span className="font-medium text-navy">Objetivo {i + 1}</span>
            <span className="max-w-[240px] truncate" title={o.text}>
              {o.text}
            </span>
          </li>
        ))}
        <li className="inline-flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-full border-2 border-slate-400 bg-white" />
          Meta al cierre (S{cycle.totalSessions})
        </li>
      </ul>

      <div className="h-[260px] w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 16, right: 28, left: -8, bottom: 0 }}>
            <CartesianGrid vertical={false} stroke="#E9EEF2" />
            <XAxis
              dataKey="session"
              tick={{ fontSize: 11, fill: "#64748B" }}
              axisLine={{ stroke: "#E2E8F0" }}
              tickLine={false}
            />
            <YAxis
              domain={[0, 100]}
              ticks={[0, 25, 50, 75, 100]}
              tickFormatter={(v) => `${v}%`}
              tick={{ fontSize: 11, fill: "#64748B" }}
              axisLine={false}
              tickLine={false}
              width={48}
            />
            <Tooltip
              content={<ChartTooltip objectives={objectives} sessions={sessions} />}
              cursor={{ stroke: "#CBD5E1", strokeWidth: 1 }}
            />
            {cycle.status !== "closed" && (
              <ReferenceLine
                x={`S${cycle.currentSession}`}
                stroke="#94A3B8"
                strokeDasharray="4 3"
                label={{ value: "Sesión actual", position: "top", fontSize: 10, fill: "#64748B" }}
              />
            )}
            {objectives.map((o, i) => (
              <ReferenceDot
                key={`target-${o.id}`}
                x={`S${cycle.totalSessions}`}
                y={o.target}
                r={5}
                fill="#fff"
                stroke={seriesColor(i)}
                strokeWidth={2}
                ifOverflow="extendDomain"
              />
            ))}
            {objectives.map((o, i) => {
              const color = seriesColor(i);
              const last = lastIndex(o.id);
              return (
                <Line
                  key={o.id}
                  type="monotone"
                  dataKey={o.id}
                  name={`Objetivo ${i + 1}`}
                  stroke={color}
                  strokeWidth={2}
                  dot={{ r: 4, strokeWidth: 2, stroke: "#fff", fill: color }}
                  activeDot={{ r: 6, strokeWidth: 2, stroke: "#fff", fill: color }}
                  connectNulls
                  isAnimationActive={false}
                >
                  <LabelList
                    dataKey={o.id}
                    content={(props) =>
                      props.index === last && props.value !== null && props.value !== undefined ? (
                        <text
                          x={props.x + 9}
                          y={props.y + 4}
                          fontSize={11}
                          fontWeight={600}
                          fill="#0B3C5D"
                        >
                          {props.value}%
                        </text>
                      ) : null
                    }
                  />
                </Line>
              );
            })}
          </LineChart>
        </ResponsiveContainer>
      </div>
      <p className="mt-1 text-[11px] text-slate-400">
        Pasa el cursor sobre cada sesión para ver el detalle. Los valores también están en el historial de
        evolución.
      </p>
    </div>
  );
}
