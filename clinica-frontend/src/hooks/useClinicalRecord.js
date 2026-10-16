import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import * as api from "../api/clinicaApi";
import { ApiError, errorMessage } from "../api/client";
import { successRate, todayIso } from "../utils/progress";

const EMPTY_EVALUATION = { hits: "", total: "10" };
/** Espera antes de enviar el PATCH de línea base / meta mientras el usuario sigue escribiendo. */
const PATCH_DEBOUNCE_MS = 600;

/** Crea el borrador vacío de la sesión indicada a partir de los objetivos activos. */
function buildDraft(sessionNumber, objectives) {
  const evaluations = {};
  objectives.forEach((o) => {
    evaluations[o.id] = { ...EMPTY_EVALUATION };
  });
  return {
    number: sessionNumber,
    date: todayIso(),
    evaluations,
    activity: "",
    achievement: "",
    observations: "",
  };
}

/** Alinea las evaluaciones del borrador con los objetivos activos (conserva lo ya escrito). */
function syncDraftObjectives(draft, objectives) {
  if (!draft) return draft;
  const evaluations = {};
  objectives.forEach((o) => {
    evaluations[o.id] = draft.evaluations[o.id] ?? { ...EMPTY_EVALUATION };
  });
  return { ...draft, evaluations };
}

/** GET /pacientes/{id}/ciclos y GET /ciclos/{id} devuelven { ...cycle, objectives }. */
function splitCycle(dto) {
  const { objectives = [], ...cycle } = dto;
  return { cycle, objectives };
}

/** GET /especialidades devuelve una lista; los componentes esperan el mapa SPECIALTIES[id]. */
function toSpecialtyMap(list) {
  return Object.fromEntries((list ?? []).map((s) => [s.id, s]));
}

const failure = (err) => ({ ok: false, message: errorMessage(err) });

/**
 * GET /pacientes/{id}/ciclos responde 404 cuando el paciente no tiene un ciclo abierto de la
 * especialidad (p. ej. tras guardar la sesión 12). No es un fallo de la ficha: se ofrece abrir uno.
 */
const isNoOpenCycle = (err) => err instanceof ApiError && err.status === 404;

/**
 * Estado de la ficha de evolución conectado a la API REST (clinica-backend).
 * Mantiene la misma forma de datos y acciones que el prototipo con datos mock,
 * de modo que los componentes no cambian. Las acciones que escriben en la API
 * son asíncronas y resuelven { ok, message } para el Toast.
 *
 * @param {object} options
 * @param {string} options.patientId    Paciente de la ficha (p. ej. "pac-0231").
 * @param {string} options.specialtyId  Especialidad del profesional logueado (TO, TL, TF, PS).
 * @param {(message: string, error?: unknown) => void} [options.onError]
 *        Errores de operaciones en segundo plano (p. ej. el PATCH de línea base / meta).
 */
export function useClinicalRecord({ patientId, specialtyId, onError } = {}) {
  const [status, setStatus] = useState("loading"); // loading | ready | no-cycle | error
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);
  const [draft, setDraft] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  const onErrorRef = useRef(onError);
  useEffect(() => {
    onErrorRef.current = onError;
  }, [onError]);

  const dataRef = useRef(data);
  useEffect(() => {
    dataRef.current = data;
  }, [data]);

  /** Último valor de línea base / meta escrito por el usuario y pendiente de enviar. */
  const editedObjectives = useRef(new Map());
  const patchTimers = useRef(new Map());
  const pendingPatches = useRef(new Map());

  const reportError = useCallback((err) => {
    onErrorRef.current?.(errorMessage(err), err);
  }, []);

  // ------------------------------------------------------------------ carga inicial
  useEffect(() => {
    if (!patientId) return undefined;
    let active = true;
    setStatus("loading");
    setError(null);

    (async () => {
      try {
        if (!specialtyId) {
          throw new Error(
            "El usuario no tiene una especialidad asociada. Inicia sesión como terapeuta o psicólogo para abrir la ficha de evolución."
          );
        }
        const [professional, specialtiesList, patient, cycleDto, bank, otherSpecialties, sharedNotes] =
          await Promise.all([
            api.getProfesionalActual(),
            api.getEspecialidades(),
            api.getPaciente(patientId),
            // null = sin ciclo abierto (404); cualquier otro error sí interrumpe la carga.
            api.getCicloActivo(patientId, specialtyId).catch((err) => {
              if (isNoOpenCycle(err)) return null;
              throw err;
            }),
            api.getBancoObjetivos(specialtyId),
            api.getVista360(patientId),
            api.getNotas(patientId),
          ]);
        const context = {
          professional,
          patient,
          specialties: toSpecialtyMap(specialtiesList),
          bank,
          otherSpecialties,
          sharedNotes,
        };
        if (!cycleDto) {
          // Sin ciclo abierto: la tarjeta para abrir uno ya cuenta con el paciente y el profesional.
          if (!active) return;
          setData({ ...context, cycle: null, objectives: [], sessions: [] });
          setDraft(null);
          setStatus("no-cycle");
          return;
        }
        const { cycle, objectives } = splitCycle(cycleDto);
        const sessions = await api.getSesiones(cycle.id);
        if (!active) return;
        setData({ ...context, cycle, objectives, sessions });
        setDraft(buildDraft(cycle.currentSession, objectives));
        setStatus("ready");
      } catch (err) {
        if (!active) return;
        setError(err);
        setStatus("error");
      }
    })();

    return () => {
      active = false;
    };
  }, [patientId, specialtyId, reloadKey]);

  // Al desmontar se descartan los PATCH pendientes.
  useEffect(() => {
    const timers = patchTimers.current;
    return () => {
      timers.forEach((timer) => clearTimeout(timer));
      timers.clear();
    };
  }, []);

  const reload = useCallback(() => setReloadKey((k) => k + 1), []);

  // ------------------------------------------------------------------ derivados
  const cycle = data?.cycle ?? null;
  const cycleId = cycle?.id ?? null;
  const objectives = data?.objectives ?? [];
  const specialtyBank = data?.bank ?? [];
  const specialties = data?.specialties ?? {};
  const canAddObjective = Boolean(cycle) && objectives.length < cycle.maxObjectives;
  const cycleClosed = cycle?.status === "closed";

  /** Objetivos del banco de la especialidad que aún no están activos. */
  const availableFromBank = useMemo(
    () => specialtyBank.filter((b) => !objectives.some((o) => o.bankId === b.id)),
    [specialtyBank, objectives]
  );

  /** Vuelve a leer el ciclo (objetivos incluidos) y alinea el borrador. */
  const refreshCycle = useCallback(async () => {
    if (!cycleId) return null;
    const refreshed = splitCycle(await api.getCiclo(cycleId));
    // Si mientras tanto se abrió otro ciclo (openCycle), esta relectura ya no corresponde a la ficha.
    if (dataRef.current?.cycle && dataRef.current.cycle.id !== cycleId) return refreshed;
    setData((prev) => (prev ? { ...prev, cycle: refreshed.cycle, objectives: refreshed.objectives } : prev));
    setDraft((prev) => syncDraftObjectives(prev, refreshed.objectives));
    return refreshed;
  }, [cycleId]);

  // ------------------------------------------------------------------ objetivos
  /** Activa en el ciclo un objetivo existente del banco (POST /ciclos/{id}/objetivos). */
  const addObjectiveFromBank = useCallback(
    async (bankId) => {
      if (!cycleId) return { ok: false, message: "La ficha aún se está cargando." };
      const item = specialtyBank.find((b) => b.id === bankId);
      if (!item) return { ok: false, message: "Selecciona un objetivo del banco." };
      try {
        // El límite de objetivos lo valida el backend: responde 409 con el detalle en español.
        await api.activarObjetivo(cycleId, item.id);
        await refreshCycle();
        return { ok: true, message: "Objetivo agregado al ciclo." };
      } catch (err) {
        // Si el rechazo se debe a un cambio hecho desde otra sesión, la relectura re-sincroniza la UI.
        if (err?.status === 409) await refreshCycle().catch(() => {});
        return failure(err);
      }
    },
    [cycleId, specialtyBank, refreshCycle]
  );

  /** Crea un objetivo nuevo en el banco (POST /banco-objetivos) y lo activa si hay cupo. */
  const createObjective = useCallback(
    async (text) => {
      if (!cycleId) return { ok: false, message: "La ficha aún se está cargando." };
      const clean = String(text ?? "").trim().replace(/\s+/g, " ");
      if (clean.length < 8) return { ok: false, message: "Describe el objetivo con más detalle (mín. 8 caracteres)." };

      let bankItem;
      try {
        bankItem = await api.crearObjetivoBanco(clean, specialtyId);
      } catch (err) {
        return failure(err);
      }
      const existed = specialtyBank.some((b) => b.id === bankItem.id);
      if (!existed) setData((prev) => (prev ? { ...prev, bank: [...prev.bank, bankItem] } : prev));

      if (objectives.some((o) => o.bankId === bankItem.id)) {
        return { ok: false, message: "Ese objetivo ya existe en el banco y está activo." };
      }
      if (!canAddObjective) {
        return { ok: true, message: "Guardado en el banco. No hay cupo para activarlo en este ciclo." };
      }
      try {
        await api.activarObjetivo(cycleId, bankItem.id);
        await refreshCycle();
        return {
          ok: true,
          message: existed
            ? "El objetivo ya existía en el banco; se activó en el ciclo."
            : "Objetivo creado, guardado en el banco y activado en el ciclo.",
        };
      } catch (err) {
        if (err?.status === 409) await refreshCycle().catch(() => {});
        return { ok: false, message: `Se guardó en el banco, pero no se pudo activar: ${errorMessage(err)}` };
      }
    },
    [cycleId, specialtyId, specialtyBank, objectives, canAddObjective, refreshCycle]
  );

  /** Retira el objetivo del ciclo (DELETE /ciclos/{id}/objetivos/{objetivoId}). */
  const removeObjective = useCallback(
    async (objectiveId) => {
      if (!cycleId) return { ok: false, message: "La ficha aún se está cargando." };
      try {
        await api.retirarObjetivo(cycleId, objectiveId);
      } catch (err) {
        return failure(err);
      }
      editedObjectives.current.delete(objectiveId);
      const timer = patchTimers.current.get(objectiveId);
      if (timer) {
        clearTimeout(timer);
        patchTimers.current.delete(objectiveId);
      }
      setData((prev) => (prev ? { ...prev, objectives: prev.objectives.filter((o) => o.id !== objectiveId) } : prev));
      setDraft((prev) => {
        if (!prev) return prev;
        const evaluations = { ...prev.evaluations };
        delete evaluations[objectiveId];
        return { ...prev, evaluations };
      });
      try {
        await refreshCycle();
      } catch (err) {
        reportError(err);
      }
      return { ok: true, message: "Objetivo retirado del ciclo." };
    },
    [cycleId, refreshCycle, reportError]
  );

  /** Envía a la API la línea base / meta pendiente de un objetivo (PATCH). */
  const flushObjectivePatch = useCallback(
    (objectiveId) => {
      const timer = patchTimers.current.get(objectiveId);
      if (timer) {
        clearTimeout(timer);
        patchTimers.current.delete(objectiveId);
      }
      const values = editedObjectives.current.get(objectiveId);
      editedObjectives.current.delete(objectiveId);
      if (!values || !cycleId) return Promise.resolve();

      const promise = api
        .ajustarObjetivo(cycleId, objectiveId, values)
        .then((updated) => {
          // Si el usuario siguió escribiendo, el siguiente PATCH traerá el valor definitivo.
          if (editedObjectives.current.has(objectiveId)) return;
          setData((prev) =>
            prev
              ? { ...prev, objectives: prev.objectives.map((o) => (o.id === objectiveId ? { ...o, ...updated } : o)) }
              : prev
          );
        })
        .catch(async (err) => {
          reportError(err);
          // Se descarta el valor rechazado y se recupera el guardado en la API.
          editedObjectives.current.delete(objectiveId);
          await refreshCycle().catch(() => {});
        })
        .finally(() => {
          if (pendingPatches.current.get(objectiveId) === promise) pendingPatches.current.delete(objectiveId);
        });
      pendingPatches.current.set(objectiveId, promise);
      return promise;
    },
    [cycleId, refreshCycle, reportError]
  );

  /** Espera a que terminen todos los PATCH pendientes (antes de guardar la sesión). */
  const flushAllObjectivePatches = useCallback(async () => {
    const ids = [...new Set([...patchTimers.current.keys(), ...editedObjectives.current.keys()])];
    await Promise.all([...ids.map(flushObjectivePatch), ...pendingPatches.current.values()]);
  }, [flushObjectivePatch]);

  /**
   * Ajusta línea base / meta: actualiza la UI al instante y envía el PATCH con un pequeño
   * retardo (el input dispara un cambio por tecla). Si la API lo rechaza (p. ej. meta ≤ línea
   * base) se avisa por onError y se restaura el valor guardado.
   */
  const updateObjective = useCallback(
    (objectiveId, patch) => {
      const current =
        editedObjectives.current.get(objectiveId) ?? dataRef.current?.objectives.find((o) => o.id === objectiveId);
      if (!current) return;
      editedObjectives.current.set(objectiveId, {
        baseline: patch.baseline ?? current.baseline,
        target: patch.target ?? current.target,
      });
      setData((prev) =>
        prev ? { ...prev, objectives: prev.objectives.map((o) => (o.id === objectiveId ? { ...o, ...patch } : o)) } : prev
      );
      const timers = patchTimers.current;
      if (timers.has(objectiveId)) clearTimeout(timers.get(objectiveId));
      timers.set(
        objectiveId,
        setTimeout(() => flushObjectivePatch(objectiveId), PATCH_DEBOUNCE_MS)
      );
    },
    [flushObjectivePatch]
  );

  // ------------------------------------------------------------------ borrador de la sesión en curso
  const updateDraft = useCallback((patch) => {
    setDraft((prev) => (prev ? { ...prev, ...patch } : prev));
  }, []);

  const updateEvaluation = useCallback((objectiveId, patch) => {
    setDraft((prev) =>
      prev
        ? {
            ...prev,
            evaluations: {
              ...prev.evaluations,
              [objectiveId]: { ...(prev.evaluations[objectiveId] ?? EMPTY_EVALUATION), ...patch },
            },
          }
        : prev
    );
  }, []);

  const resetDraft = useCallback(() => {
    if (cycle) setDraft(buildDraft(cycle.currentSession, objectives));
  }, [cycle, objectives]);

  /** Validación mínima del formulario de evolución (la API vuelve a validar). */
  const draftErrors = useMemo(() => {
    const errors = {};
    if (!draft) return errors;
    objectives.forEach((o) => {
      const ev = draft.evaluations[o.id];
      if (successRate(ev) === null) errors[o.id] = "Registra aciertos e intentos.";
      else if (Number(ev.hits) > Number(ev.total)) errors[o.id] = "Los aciertos no pueden superar los intentos.";
    });
    if (!draft.activity.trim()) errors.activity = "Describe la actividad realizada.";
    if (!draft.achievement.trim()) errors.achievement = "Registra el logro de la sesión.";
    return errors;
  }, [draft, objectives]);

  /**
   * Guarda la sesión en curso (POST /ciclos/{id}/sesiones) y refresca ciclo + sesiones.
   * Al guardar la 12.ª sesión el backend cierra el ciclo.
   */
  const saveSession = useCallback(async () => {
    if (!cycle || !draft) return { ok: false, message: "La ficha aún se está cargando." };
    if (cycleClosed) return { ok: false, message: "El ciclo ya está cerrado." };
    if (Object.keys(draftErrors).length) {
      return { ok: false, message: "Completa los campos obligatorios antes de guardar." };
    }
    await flushAllObjectivePatches();

    const payload = {
      number: draft.number,
      date: draft.date,
      evaluations: Object.fromEntries(
        objectives.map((o) => {
          const ev = draft.evaluations[o.id];
          return [o.id, { hits: Number(ev.hits), total: Number(ev.total) }];
        })
      ),
      activity: draft.activity.trim(),
      achievement: draft.achievement.trim(),
      observations: draft.observations.trim(),
    };
    try {
      await api.registrarSesion(cycle.id, payload);
    } catch (err) {
      return failure(err);
    }

    try {
      const [cycleDto, sessions] = await Promise.all([api.getCiclo(cycle.id), api.getSesiones(cycle.id)]);
      const refreshed = splitCycle(cycleDto);
      setData((prev) => (prev ? { ...prev, cycle: refreshed.cycle, objectives: refreshed.objectives, sessions } : prev));
      setDraft(buildDraft(refreshed.cycle.currentSession, refreshed.objectives));
      if (refreshed.cycle.status === "closed") {
        return {
          ok: true,
          message: `Sesión ${draft.number} guardada. Ciclo de ${refreshed.cycle.totalSessions} semanas cerrado: revisa la escala GAS.`,
        };
      }
      return { ok: true, message: `Sesión ${draft.number} guardada correctamente.` };
    } catch (err) {
      return {
        ok: false,
        message: `La sesión ${draft.number} se guardó, pero no se pudo actualizar la ficha: ${errorMessage(err)}`,
      };
    }
  }, [cycle, draft, cycleClosed, draftErrors, objectives, flushAllObjectivePatches]);

  // ------------------------------------------------------------------ nuevo ciclo
  /** Descarta los PATCH de línea base / meta aún no enviados (al cambiar de ciclo ya no aplican). */
  const discardPendingPatches = useCallback(() => {
    patchTimers.current.forEach((timer) => clearTimeout(timer));
    patchTimers.current.clear();
    editedObjectives.current.clear();
  }, []);

  /**
   * Abre un nuevo ciclo de la especialidad (POST /pacientes/{id}/ciclos) cuando el paciente no tiene
   * uno abierto (status "no-cycle") o el actual ya se cerró, y deja la ficha en la sesión 1 sin objetivos.
   * Si la API responde 409 (ya hay un ciclo abierto, p. ej. creado desde otra pestaña) se recarga la ficha.
   */
  const openCycle = useCallback(
    async ({ startDate, frequency, totalSessions } = {}) => {
      if (!patientId || !specialtyId) {
        return { ok: false, message: "No se puede abrir un ciclo sin paciente ni especialidad." };
      }
      if (cycle && !cycleClosed) {
        return { ok: false, message: `El ciclo ${cycle.id} sigue abierto; no se puede abrir otro.` };
      }
      const body = { specialtyId };
      if (startDate) body.startDate = startDate;
      const cleanFrequency = String(frequency ?? "").trim().replace(/\s+/g, " ");
      if (cleanFrequency) body.frequency = cleanFrequency;
      if (Number(totalSessions) > 0) body.totalSessions = Number(totalSessions);

      let createdDto;
      try {
        createdDto = await api.crearCiclo(patientId, body);
      } catch (err) {
        if (err?.status === 409) reload();
        return failure(err);
      }

      discardPendingPatches();
      const created = splitCycle(createdDto);
      try {
        const sessions = await api.getSesiones(created.cycle.id);
        setData((prev) =>
          prev ? { ...prev, cycle: created.cycle, objectives: created.objectives, sessions } : prev
        );
        setDraft(buildDraft(created.cycle.currentSession, created.objectives));
        setStatus("ready");
        return {
          ok: true,
          message: `Ciclo ${created.cycle.id} abierto: sesión 1 de ${created.cycle.totalSessions}. Agrega hasta ${created.cycle.maxObjectives} objetivos del banco.`,
        };
      } catch (err) {
        reload();
        return {
          ok: false,
          message: `El ciclo ${created.cycle.id} se abrió, pero no se pudo actualizar la ficha: ${errorMessage(err)}`,
        };
      }
    },
    [patientId, specialtyId, cycle, cycleClosed, reload, discardPendingPatches]
  );

  // ------------------------------------------------------------------ reporte
  /** Descarga el reporte PDF del ciclo (GET /ciclos/{id}/reporte.pdf) con el token. */
  const fetchReportPdf = useCallback(async () => {
    if (!cycleId) throw new Error("La ficha aún se está cargando.");
    const { blob, filename } = await api.descargarReportePdf(cycleId);
    return { blob, filename: filename ?? `ficha-evolucion-${cycleId}.pdf` };
  }, [cycleId]);

  const professional = data?.professional ?? null;
  const specialty = specialties[specialtyId] ?? (professional ? specialties[professional.specialtyId] : undefined) ?? null;

  return {
    status,
    error,
    professional,
    patient: data?.patient ?? null,
    specialty,
    specialties,
    cycle,
    cycleClosed,
    objectives,
    sessions: data?.sessions ?? [],
    draft,
    draftErrors,
    availableFromBank,
    specialtyBank,
    canAddObjective,
    otherSpecialties: data?.otherSpecialties ?? [],
    sharedNotes: data?.sharedNotes ?? [],
    actions: {
      addObjectiveFromBank,
      createObjective,
      removeObjective,
      updateObjective,
      updateDraft,
      updateEvaluation,
      saveSession,
      resetDraft,
      fetchReportPdf,
      openCycle,
      reload,
    },
  };
}
