import { useState } from "react";
import { Check, CircleAlert, ListChecks, Plus, Target, Trash2, X } from "lucide-react";
import Badge from "../ui/Badge";
import Button from "../ui/Button";
import Card from "../ui/Card";
import { lastScore } from "../../utils/progress";
import { seriesColor } from "../../utils/theme";

const clampPct = (value) => Math.max(0, Math.min(100, Number(value) || 0));

function ObjectiveCard({ objective, index, sessions, onRemove, onUpdate }) {
  const [confirming, setConfirming] = useState(false);
  const last = lastScore(sessions, objective.id);
  const color = seriesColor(index);

  return (
    <li className="flex gap-3 rounded-xl border border-slate-200 bg-white p-4 transition hover:shadow-card">
      <span
        className="mt-0.5 grid h-7 w-7 shrink-0 place-items-center rounded-full text-xs font-bold text-white"
        style={{ backgroundColor: color }}
      >
        {index + 1}
      </span>

      <div className="min-w-0 flex-1">
        <p className="font-medium leading-snug text-navy">{objective.text}</p>
        <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs text-slate-600">
          <label className="inline-flex items-center gap-1.5">
            Línea base
            <input
              type="number"
              min={0}
              max={100}
              value={objective.baseline}
              onChange={(e) => onUpdate(objective.id, { baseline: clampPct(e.target.value) })}
              className="input h-7 w-16 px-2 py-0 text-xs"
              aria-label="Línea base (%)"
            />
            %
          </label>
          <label className="inline-flex items-center gap-1.5">
            <Target size={13} className="text-primary-500" />
            Meta
            <input
              type="number"
              min={0}
              max={100}
              value={objective.target}
              onChange={(e) => onUpdate(objective.id, { target: clampPct(e.target.value) })}
              className="input h-7 w-16 px-2 py-0 text-xs"
              aria-label="Meta del ciclo (%)"
            />
            %
          </label>
          <span className="text-slate-400">Desde la sesión {objective.addedInSession}</span>
          {last !== null && <Badge tone="primary">Última sesión: {last}%</Badge>}
        </div>
      </div>

      <div className="no-print shrink-0">
        {confirming ? (
          <div className="flex items-center gap-1">
            <Button size="sm" variant="danger" icon={Check} onClick={() => onRemove(objective.id)}>
              Retirar
            </Button>
            <Button size="sm" variant="ghost" icon={X} onClick={() => setConfirming(false)} aria-label="Cancelar" />
          </div>
        ) : (
          <Button
            size="icon"
            variant="ghost"
            icon={Trash2}
            onClick={() => setConfirming(true)}
            aria-label="Retirar objetivo del ciclo"
            title="Retirar objetivo del ciclo"
          />
        )}
      </div>
    </li>
  );
}

export default function ObjectivesPanel({
  objectives,
  availableFromBank,
  specialty,
  canAdd,
  maxObjectives,
  sessions,
  onAddFromBank,
  onCreate,
  onRemove,
  onUpdate,
  notify,
}) {
  const [selected, setSelected] = useState("");
  const [newText, setNewText] = useState("");
  const [busy, setBusy] = useState(false);

  // Las acciones llaman a la API y resuelven { ok, message }; mientras tanto se bloquean los botones.
  const handleAdd = async () => {
    if (busy) return;
    setBusy(true);
    try {
      const res = await onAddFromBank(selected);
      notify(res);
      if (res.ok) setSelected("");
    } finally {
      setBusy(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    try {
      const res = await onCreate(newText);
      notify(res);
      if (res.ok) setNewText("");
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card
      id="objetivos"
      title="Gestión de objetivos"
      subtitle={`Máximo ${maxObjectives} objetivos por ciclo de 12 semanas · Banco de ${specialty.name}`}
      icon={ListChecks}
      actions={
        <Badge tone={canAdd ? "primary" : "warning"}>
          {objectives.length} / {maxObjectives} activos
        </Badge>
      }
    >
      <div className="no-print grid gap-4 md:grid-cols-2">
        <div>
          <label className="label" htmlFor="bank-select">
            Seleccionar del banco de objetivos
          </label>
          <div className="flex gap-2">
            <select
              id="bank-select"
              className="input"
              value={selected}
              onChange={(e) => setSelected(e.target.value)}
              disabled={!canAdd}
            >
              <option value="">
                {availableFromBank.length ? "Elige un objetivo…" : "No quedan objetivos disponibles"}
              </option>
              {availableFromBank.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.text}
                </option>
              ))}
            </select>
            <Button icon={Plus} onClick={handleAdd} disabled={!canAdd || !selected || busy}>
              Agregar
            </Button>
          </div>
          <p className="mt-1 text-[11px] text-slate-500">
            {availableFromBank.length} objetivos disponibles en el banco de {specialty.short}.
          </p>
        </div>

        <form onSubmit={handleCreate}>
          <label className="label" htmlFor="new-objective">
            ¿No está en el banco? Crea uno nuevo
          </label>
          <div className="flex gap-2">
            <input
              id="new-objective"
              className="input"
              placeholder="Ej. Enhebrar cuentas pequeñas en un cordón"
              value={newText}
              onChange={(e) => setNewText(e.target.value)}
              maxLength={160}
            />
            <Button
              type="submit"
              variant="accent"
              size="icon"
              icon={Plus}
              aria-label="Crear objetivo y guardarlo en el banco"
              title="Crear objetivo y guardarlo en el banco"
              disabled={newText.trim().length < 8 || busy}
            />
          </div>
          <p className="mt-1 text-[11px] text-slate-500">
            {canAdd
              ? `Se guardará en el banco de ${specialty.short} y se activará en este ciclo.`
              : `Sin cupo en este ciclo: se guardará en el banco de ${specialty.short} para futuros usos.`}
          </p>
        </form>
      </div>

      {!canAdd && (
        <div className="no-print mt-4 flex items-start gap-2 rounded-lg border border-accent-100 bg-accent-50 px-3 py-2 text-xs text-accent-700">
          <CircleAlert size={14} className="mt-0.5 shrink-0" />
          Límite de {maxObjectives} objetivos alcanzado para este ciclo. Retira uno para agregar otro o planifícalo para
          el siguiente ciclo.
        </div>
      )}

      <ul className="mt-5 space-y-3">
        {objectives.map((o, i) => (
          <ObjectiveCard
            key={o.id}
            objective={o}
            index={i}
            sessions={sessions}
            onRemove={onRemove}
            onUpdate={onUpdate}
          />
        ))}
        {!objectives.length && (
          <li className="rounded-xl border border-dashed border-slate-300 p-6 text-center text-sm text-slate-500">
            Aún no hay objetivos activos. Selecciona uno del banco o crea uno nuevo.
          </li>
        )}
      </ul>
    </Card>
  );
}
