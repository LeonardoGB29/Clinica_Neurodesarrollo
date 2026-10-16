import { useState } from "react";
import { CalendarPlus } from "lucide-react";
import Button from "../ui/Button";
import { todayIso } from "../../utils/progress";

/** Valores por defecto de un ciclo nuevo (coinciden con los del backend). */
export const DEFAULT_TOTAL_SESSIONS = 12;
export const DEFAULT_FREQUENCY = "1 sesión / semana · 45 min";
/** CrearCicloRequest.frequency admite hasta 60 caracteres. */
const FREQUENCY_MAX_LENGTH = 60;

/**
 * Formulario para abrir un ciclo terapéutico: fecha de inicio (hoy por defecto) y frecuencia.
 * Se usa en la tarjeta "Sin ciclo abierto" y en el bloque "Ciclo completado" del historial.
 * `onOpen({ startDate, frequency, totalSessions })` hace el POST y resuelve { ok, message };
 * el padre muestra el resultado en el Toast y pasa `busy` mientras la API responde.
 */
export default function NewCycleForm({
  idPrefix = "nuevo-ciclo",
  specialty,
  totalSessions = DEFAULT_TOTAL_SESSIONS,
  defaultFrequency = DEFAULT_FREQUENCY,
  onOpen,
  busy = false,
}) {
  const [startDate, setStartDate] = useState(todayIso);
  const [frequency, setFrequency] = useState(defaultFrequency || DEFAULT_FREQUENCY);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (busy || !startDate) return;
    onOpen({ startDate, frequency, totalSessions });
  };

  return (
    <form className="grid gap-3 sm:grid-cols-2" onSubmit={handleSubmit} noValidate>
      <div>
        <label className="label" htmlFor={`${idPrefix}-fecha`}>
          Fecha de inicio
        </label>
        <input
          id={`${idPrefix}-fecha`}
          type="date"
          className="input"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          disabled={busy}
        />
      </div>
      <div>
        <label className="label" htmlFor={`${idPrefix}-frecuencia`}>
          Frecuencia
        </label>
        <input
          id={`${idPrefix}-frecuencia`}
          className="input"
          value={frequency}
          maxLength={FREQUENCY_MAX_LENGTH}
          placeholder={DEFAULT_FREQUENCY}
          onChange={(e) => setFrequency(e.target.value)}
          disabled={busy}
        />
      </div>
      <div className="flex flex-wrap items-center justify-between gap-3 sm:col-span-2">
        <p className="text-xs text-slate-500">
          El ciclo empieza en la sesión 1 de {totalSessions} sin objetivos activos; agrégalos después desde el
          banco{specialty ? ` de ${specialty.short}` : ""}.
        </p>
        <Button
          type="submit"
          variant="primary"
          icon={CalendarPlus}
          disabled={busy || !startDate}
          className="whitespace-nowrap"
        >
          {busy ? "Abriendo ciclo…" : `Abrir nuevo ciclo de ${totalSessions} sesiones`}
        </Button>
      </div>
    </form>
  );
}
