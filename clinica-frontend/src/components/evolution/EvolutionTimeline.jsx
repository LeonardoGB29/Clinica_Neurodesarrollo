import { useState } from "react";
import { CalendarPlus, ChevronsDownUp, ChevronsUpDown, CircleCheck, History } from "lucide-react";
import NewCycleForm from "../cycle/NewCycleForm";
import Button from "../ui/Button";
import Card from "../ui/Card";
import EvolutionForm from "./EvolutionForm";
import SessionRecord from "./SessionRecord";

/**
 * Historial de evolución en cascada: sesiones registradas (solo lectura)
 * seguidas del formulario editable de la sesión en curso.
 */
export default function EvolutionTimeline({
  sessions,
  objectives,
  draft,
  cycle,
  cycleClosed,
  specialty,
  errors,
  showErrors,
  actions,
  onSave,
  onOpenCycle,
  openingCycle = false,
}) {
  const [collapsed, setCollapsed] = useState({});
  const allCollapsed = sessions.length > 0 && sessions.every((s) => collapsed[s.number]);

  const toggle = (n) => setCollapsed((c) => ({ ...c, [n]: !c[n] }));
  const toggleAll = () => {
    const next = {};
    if (!allCollapsed) sessions.forEach((s) => (next[s.number] = true));
    setCollapsed(next);
  };

  const subtitle = `Ficha en cascada: ${sessions.length} ${
    sessions.length === 1 ? "sesión registrada" : "sesiones registradas"
  } (solo lectura)${cycleClosed ? "" : " y la sesión en curso"}`;

  return (
    <Card
      id="historial"
      title="Historial de evolución"
      subtitle={subtitle}
      icon={History}
      actions={
        sessions.length > 0 && (
          <Button
            variant="ghost"
            size="sm"
            icon={allCollapsed ? ChevronsUpDown : ChevronsDownUp}
            onClick={toggleAll}
            className="no-print"
          >
            {allCollapsed ? "Expandir todo" : "Contraer todo"}
          </Button>
        )
      }
    >
      <ol className="relative space-y-4 before:absolute before:bottom-3 before:left-4 before:top-3 before:w-0.5 before:bg-slate-200">
        {sessions.map((s) => (
          <SessionRecord
            key={s.number}
            session={s}
            objectives={objectives}
            expanded={!collapsed[s.number]}
            onToggle={() => toggle(s.number)}
          />
        ))}

        {cycleClosed ? (
          <li className="relative pl-11">
            <span className="absolute left-0 top-1 grid h-8 w-8 place-items-center rounded-full bg-emerald-500 text-white ring-4 ring-white">
              <CircleCheck size={16} />
            </span>
            <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4">
              <p className="font-semibold text-emerald-800">Ciclo de 12 semanas completado</p>
              <p className="mt-1 text-sm text-emerald-700">
                Las {cycle.totalSessions} sesiones fueron registradas. Revisa la escala GAS en el dashboard para
                decidir si se sube, mantiene o baja la carga de cada objetivo en el siguiente ciclo.
              </p>

              {onOpenCycle && (
                /* Mismo flujo que la tarjeta "Sin ciclo abierto": POST /pacientes/{id}/ciclos */
                <div id="nuevo-ciclo" className="no-print mt-4 rounded-xl border border-emerald-200 bg-white p-4">
                  <p className="inline-flex items-center gap-1.5 font-semibold text-navy">
                    <CalendarPlus size={16} className="text-primary-500" />
                    Siguiente ciclo{specialty ? ` de ${specialty.name}` : ""}
                  </p>
                  <p className="mt-1 text-xs text-slate-500">
                    Abre el nuevo ciclo con la fecha de la primera sesión; los objetivos se vuelven a definir según la
                    decisión GAS (subir, mantener o bajar carga).
                  </p>
                  <div className="mt-3">
                    <NewCycleForm
                      idPrefix="nuevo-ciclo"
                      specialty={specialty}
                      totalSessions={cycle.totalSessions}
                      defaultFrequency={cycle.frequency}
                      onOpen={onOpenCycle}
                      busy={openingCycle}
                    />
                  </div>
                </div>
              )}
            </div>
          </li>
        ) : (
          <EvolutionForm
            draft={draft}
            objectives={objectives}
            errors={errors}
            showErrors={showErrors}
            totalSessions={cycle.totalSessions}
            onChange={actions.updateDraft}
            onEvalChange={actions.updateEvaluation}
            onSave={onSave}
            onReset={actions.resetDraft}
          />
        )}
      </ol>
    </Card>
  );
}
