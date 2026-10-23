import { useCallback, useState } from "react";
import { Orbit } from "lucide-react";
import logo from "./assets/logo-horizontal.svg";
import { API_URL, errorMessage } from "./api/client";
import LoginScreen from "./components/auth/LoginScreen";
import NoCycleCard from "./components/cycle/NoCycleCard";
import ProgressDashboard from "./components/dashboard/ProgressDashboard";
import EvolutionTimeline from "./components/evolution/EvolutionTimeline";
import SessionHeader from "./components/header/SessionHeader";
import View360 from "./components/interdisciplinary/View360";
import Sidebar from "./components/layout/Sidebar";
import Topbar from "./components/layout/Topbar";
import ObjectivesPanel from "./components/objectives/ObjectivesPanel";
import Sheet from "./components/ui/Sheet";
import { ErrorScreen, LoadingScreen } from "./components/ui/StatusScreen";
import { ToastStack, useToast } from "./components/ui/Toast";
import { useAuth } from "./hooks/useAuth";
import { useClinicalRecord } from "./hooks/useClinicalRecord";
import { formatDate, todayIso } from "./utils/progress";

/** Paciente de la ficha. En el siguiente hito vendrá de la navegación (módulo Pacientes). */
const PATIENT_ID = import.meta.env.VITE_PACIENTE_ID || "pac-0231";

/**
 * Raíz de la aplicación: sesión (JWT) + notificaciones.
 * Sin sesión se muestra el login; con sesión, la ficha de evolución conectada a la API.
 */
export default function App() {
  const { session, notice, login, logout } = useAuth();
  const { toasts, push, dismiss } = useToast();

  return (
    <>
      {session ? (
        <ClinicalRecordPage key={session.usuario?.id ?? "sesion"} session={session} onLogout={logout} push={push} />
      ) : (
        <LoginScreen onLogin={login} notice={notice} />
      )}
      <ToastStack toasts={toasts} onDismiss={dismiss} />
    </>
  );
}

/**
 * Vista principal: Ficha de Evolución + Dashboard de progreso + Vista 360°.
 * Todo el estado vive en useClinicalRecord (datos de la API REST).
 */
function ClinicalRecordPage({ session, onLogout, push }) {
  const onError = useCallback((message) => push(message, "error"), [push]);
  const record = useClinicalRecord({
    patientId: PATIENT_ID,
    specialtyId: session.usuario?.specialtyId ?? null,
    onError,
  });
  const [showErrors, setShowErrors] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [openingCycle, setOpeningCycle] = useState(false);
  /** Vista 360° en un panel lateral deslizable, para no sobrecargar la ficha. */
  const [view360Open, setView360Open] = useState(false);
  const open360 = useCallback(() => setView360Open(true), []);
  const close360 = useCallback(() => setView360Open(false), []);

  const notify = (result) => push(result.message, result.ok ? "success" : "error");

  const handleSave = async () => {
    if (saving) return;
    setSaving(true);
    try {
      const result = await record.actions.saveSession();
      setShowErrors(!result.ok);
      notify(result);
      if (!result.ok) {
        document.getElementById("evolution-form")?.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    } finally {
      setSaving(false);
    }
  };

  /** Abre GET /ciclos/{id}/reporte.pdf: la pestaña se abre en el clic (evita el bloqueo de ventanas emergentes). */
  const handleGeneratePdf = async () => {
    const popup = window.open("", "_blank");
    if (popup) {
      popup.document.title = "Reporte PDF · Ficha de evolución";
      popup.document.body.innerHTML =
        '<p style="font-family:Inter,system-ui,sans-serif;padding:2rem;color:#0B3C5D">Generando el reporte PDF de la ficha de evolución…</p>';
    }
    push("Generando el reporte PDF de la ficha de evolución…", "info");
    try {
      const { blob, filename } = await record.actions.fetchReportPdf();
      const url = URL.createObjectURL(blob);
      if (popup && !popup.closed) {
        popup.location.href = url;
      } else {
        const link = document.createElement("a");
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();
      }
      window.setTimeout(() => URL.revokeObjectURL(url), 10 * 60 * 1000);
    } catch (err) {
      popup?.close();
      push(`No se pudo generar el reporte PDF: ${errorMessage(err)}`, "error");
    }
  };

  const handleRemoveObjective = async (objectiveId) => {
    const result = await record.actions.removeObjective(objectiveId);
    push(result.message, result.ok ? "info" : "error");
  };

  /**
   * Abre un nuevo ciclo (POST /pacientes/{id}/ciclos) desde la tarjeta "Sin ciclo abierto" o desde el
   * bloque "Ciclo completado" del historial. El resultado (201 / 409 / 403…) se muestra en el Toast.
   */
  const handleOpenCycle = async (values) => {
    if (openingCycle) return;
    setOpeningCycle(true);
    try {
      const result = await record.actions.openCycle(values);
      notify(result);
      if (result.ok) {
        setShowErrors(false);
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
    } finally {
      setOpeningCycle(false);
    }
  };

  /** Con el ciclo cerrado, el botón de la cabecera lleva al formulario de nuevo ciclo del historial. */
  const focusNewCycleForm = () => {
    document.getElementById("nuevo-ciclo")?.scrollIntoView({ behavior: "smooth", block: "center" });
    document.getElementById("nuevo-ciclo-fecha")?.focus({ preventScroll: true });
  };

  if (record.status === "loading") return <LoadingScreen />;
  if (record.status === "error") {
    return <ErrorScreen error={record.error} onRetry={record.actions.reload} onLogout={onLogout} />;
  }
  /** 404 en GET /pacientes/{id}/ciclos: no hay ciclo abierto de la especialidad → tarjeta para abrir uno. */
  const noCycle = record.status === "no-cycle";

  return (
    <div className="flex min-h-screen bg-surface">
      <Sidebar
        professional={record.professional}
        specialty={record.specialty}
        open={menuOpen}
        onClose={() => setMenuOpen(false)}
        onLogout={onLogout}
        onOpen360={open360}
      />

      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar
          professional={record.professional}
          specialty={record.specialty}
          patient={record.patient}
          onMenu={() => setMenuOpen(true)}
          onOpen360={open360}
        />

        <main className="mx-auto w-full max-w-[1360px] flex-1 space-y-6 px-4 py-6 lg:px-6 print:max-w-none print:p-0">
          {noCycle ? (
            <div className="mx-auto w-full max-w-5xl">
              <NoCycleCard
                patient={record.patient}
                professional={record.professional}
                specialty={record.specialty}
                onOpen={handleOpenCycle}
                busy={openingCycle}
              />
            </div>
          ) : (
            <>
              {/* Cabecera visible solo en el reporte impreso / PDF */}
              <div className="hidden items-center justify-between border-b border-slate-200 pb-3 print:flex">
                <img src={logo} alt="Clínica San Juan de Dios" className="h-10 w-auto" />
                <div className="text-right text-xs text-slate-500">
                  <p className="font-semibold text-navy">Reporte de Ficha de Evolución · Centro de Neurodesarrollo</p>
                  <p>Generado el {formatDate(todayIso())} por {record.professional.fullName}</p>
                </div>
              </div>

              <SessionHeader
                professional={record.professional}
                specialty={record.specialty}
                patient={record.patient}
                cycle={record.cycle}
                draft={record.draft}
                cycleClosed={record.cycleClosed}
                onGeneratePdf={handleGeneratePdf}
                onSave={handleSave}
                onOpenNextCycle={focusNewCycleForm}
              />

              <div className="space-y-6">
                  <ObjectivesPanel
                    objectives={record.objectives}
                    availableFromBank={record.availableFromBank}
                    specialty={record.specialty}
                    canAdd={record.canAddObjective}
                    maxObjectives={record.cycle.maxObjectives}
                    sessions={record.sessions}
                    onAddFromBank={record.actions.addObjectiveFromBank}
                    onCreate={record.actions.createObjective}
                    onRemove={handleRemoveObjective}
                    onUpdate={record.actions.updateObjective}
                    notify={notify}
                  />

                  <ProgressDashboard objectives={record.objectives} sessions={record.sessions} cycle={record.cycle} />

                  <EvolutionTimeline
                    sessions={record.sessions}
                    objectives={record.objectives}
                    draft={record.draft}
                    cycle={record.cycle}
                    cycleClosed={record.cycleClosed}
                    specialty={record.specialty}
                    errors={record.draftErrors}
                    showErrors={showErrors}
                    actions={record.actions}
                    onSave={handleSave}
                    onOpenCycle={handleOpenCycle}
                    openingCycle={openingCycle}
                  />
              </div>
            </>
          )}

          <footer className="no-print pb-2 text-center text-[11px] text-slate-400">
            Prototipo de validación · Sistema de Gestión Clínica Interdisciplinaria · Centro de Neurodesarrollo
            (TEA/TDAH) · Conectado a la API {API_URL}
          </footer>
        </main>
      </div>

      {/* Vista 360° interdisciplinaria: panel lateral sobre la ficha (botón de la barra superior o menú) */}
      <Sheet
        open={view360Open}
        onClose={close360}
        title="Vista 360° interdisciplinaria"
        subtitle={`${record.patient.fullName} · objetivos y progreso en las demás especialidades · solo lectura`}
        icon={Orbit}
      >
        <View360
          embedded
          otherSpecialties={record.otherSpecialties ?? []}
          specialties={record.specialties ?? {}}
          currentSpecialty={record.specialty}
          objectives={record.objectives ?? []}
          sessions={record.sessions ?? []}
          sharedNotes={record.sharedNotes ?? []}
        />
      </Sheet>
    </div>
  );
}
