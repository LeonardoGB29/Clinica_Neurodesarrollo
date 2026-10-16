package pe.sanjuandedios.clinica.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.domain.BancoObjetivo;
import pe.sanjuandedios.clinica.domain.Ciclo;
import pe.sanjuandedios.clinica.domain.Especialidad;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;
import pe.sanjuandedios.clinica.domain.ObjetivoCiclo;
import pe.sanjuandedios.clinica.domain.Paciente;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.domain.ResultadoGas;
import pe.sanjuandedios.clinica.domain.Sesion;
import pe.sanjuandedios.clinica.domain.progreso.NivelGas;
import pe.sanjuandedios.clinica.domain.progreso.ProgresoService;
import pe.sanjuandedios.clinica.domain.progreso.SesionRegistrada;
import pe.sanjuandedios.clinica.dto.CicloDTO;
import pe.sanjuandedios.clinica.dto.ObjetivoActivoDTO;
import pe.sanjuandedios.clinica.dto.ProgresoCicloDTO;
import pe.sanjuandedios.clinica.dto.ProgresoCicloDTO.GasDTO;
import pe.sanjuandedios.clinica.dto.ProgresoCicloDTO.ProgresoObjetivoDTO;
import pe.sanjuandedios.clinica.dto.ProgresoCicloDTO.PuntoSerieDTO;
import pe.sanjuandedios.clinica.dto.ProgresoCicloDTO.ResultadoGasDTO;
import pe.sanjuandedios.clinica.dto.Requests.ActivarObjetivoRequest;
import pe.sanjuandedios.clinica.dto.Requests.AjustarObjetivoRequest;
import pe.sanjuandedios.clinica.dto.Requests.CrearCicloRequest;
import pe.sanjuandedios.clinica.dto.Requests.EvaluacionRequest;
import pe.sanjuandedios.clinica.dto.Requests.RegistrarSesionRequest;
import pe.sanjuandedios.clinica.dto.SesionDTO;
import pe.sanjuandedios.clinica.mapper.CicloMapper;
import pe.sanjuandedios.clinica.mapper.ObjetivoMapper;
import pe.sanjuandedios.clinica.mapper.SesionMapper;
import pe.sanjuandedios.clinica.repository.BancoObjetivoRepository;
import pe.sanjuandedios.clinica.repository.CicloRepository;
import pe.sanjuandedios.clinica.repository.EspecialidadRepository;
import pe.sanjuandedios.clinica.repository.ObjetivoCicloRepository;
import pe.sanjuandedios.clinica.repository.PacienteRepository;
import pe.sanjuandedios.clinica.repository.ProfesionalRepository;
import pe.sanjuandedios.clinica.repository.ResultadoGasRepository;
import pe.sanjuandedios.clinica.repository.SesionRepository;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;
import pe.sanjuandedios.clinica.web.error.ReglaNegocioException;
import pe.sanjuandedios.clinica.web.error.ValidacionException;

/**
 * Casos de uso del ciclo terapéutico: consulta del ciclo activo, gestión de objetivos
 * (máx. 3 activos), registro de sesiones inmutables, progreso/GAS y cierre del ciclo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CicloService {

    private static final String FRECUENCIA_POR_DEFECTO = "1 sesión / semana · 45 min";
    private static final int LINEA_BASE_POR_DEFECTO = 0;
    private static final int META_POR_DEFECTO = 80;

    private final CicloRepository cicloRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalRepository profesionalRepository;
    private final BancoObjetivoRepository bancoObjetivoRepository;
    private final ObjetivoCicloRepository objetivoCicloRepository;
    private final SesionRepository sesionRepository;
    private final ResultadoGasRepository resultadoGasRepository;
    private final ProgresoService progresoService;
    private final AutorizacionService autorizacionService;
    private final CicloMapper cicloMapper;
    private final ObjetivoMapper objetivoMapper;
    private final SesionMapper sesionMapper;

    // ------------------------------------------------------------------ consultas

    /** Ciclo abierto del paciente en la especialidad indicada, con sus objetivos activos. */
    @Transactional(readOnly = true)
    public CicloDTO obtenerActivo(String pacienteId, String especialidadId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RecursoNoEncontradoException("Paciente", pacienteId);
        }
        String esp = BancoObjetivoService.normalizarEspecialidad(especialidadId);
        Ciclo ciclo = cicloRepository.findByPacienteIdAndEspecialidadIdAndEstado(pacienteId, esp, EstadoCiclo.ABIERTO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El paciente %s no tiene un ciclo abierto de la especialidad %s.".formatted(pacienteId, esp)));
        return cicloMapper.toDto(ciclo);
    }

    @Transactional(readOnly = true)
    public CicloDTO obtener(String cicloId) {
        return cicloMapper.toDto(cargar(cicloId));
    }

    @Transactional(readOnly = true)
    public List<SesionDTO> listarSesiones(String cicloId) {
        cargar(cicloId);
        return sesionMapper.toDtoList(sesionRepository.findByCicloIdOrderByNumeroAsc(cicloId));
    }

    @Transactional(readOnly = true)
    public SesionDTO obtenerSesion(String cicloId, int numero) {
        cargar(cicloId);
        return sesionRepository.findByCicloIdAndNumero(cicloId, numero)
                .map(sesionMapper::toDto)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El ciclo %s no tiene registrada la sesión %d.".formatted(cicloId, numero)));
    }

    /** % acumulado, última sesión, variación, serie y nivel GAS de cada objetivo activo. */
    @Transactional(readOnly = true)
    public ProgresoCicloDTO progreso(String cicloId) {
        return progresoInterno(cargar(cicloId));
    }

    // ------------------------------------------------------------------ ciclo

    /** Abre un nuevo ciclo de 12 sesiones. Solo puede haber un ciclo abierto por paciente y especialidad. */
    @Transactional
    public CicloDTO crear(String pacienteId, CrearCicloRequest request, UsuarioAutenticado usuario) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", pacienteId));
        String esp = BancoObjetivoService.normalizarEspecialidad(request.specialtyId());
        Especialidad especialidad = especialidadRepository.findById(esp)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", esp));
        autorizacionService.verificarPuedeEditarEspecialidad(esp, usuario);
        cicloRepository.findByPacienteIdAndEspecialidadIdAndEstado(pacienteId, esp, EstadoCiclo.ABIERTO)
                .ifPresent(c -> {
                    throw new ReglaNegocioException("El paciente ya tiene un ciclo abierto de %s (%s)."
                            .formatted(especialidad.getNombre(), c.getId()));
                });
        Profesional profesional = resolverProfesional(request.professionalId(), usuario, null);
        if (!profesional.getEspecialidad().getId().equals(esp)) {
            throw new ReglaNegocioException("El profesional a cargo debe pertenecer a la especialidad " + esp + ".");
        }

        LocalDate inicio = request.startDate() != null ? request.startDate() : LocalDate.now();
        long correlativo = cicloRepository.countByPacienteIdAndEspecialidadId(pacienteId, esp) + 1;
        String sufijoHistoria = paciente.getNroHistoria().replaceAll("^.*-", "");
        Ciclo ciclo = Ciclo.builder()
                .id("cic-%d-%s-%s-%02d".formatted(inicio.getYear(), esp, sufijoHistoria, correlativo))
                .paciente(paciente)
                .especialidad(especialidad)
                .profesional(profesional)
                .fechaInicio(inicio)
                .totalSesiones(request.totalSessions() != null ? request.totalSessions() : Ciclo.TOTAL_SESIONES_POR_DEFECTO)
                .sesionActual(1)
                .frecuencia(request.frequency() != null && !request.frequency().isBlank() ? request.frequency().trim() : FRECUENCIA_POR_DEFECTO)
                .maxObjetivos(Ciclo.MAX_OBJETIVOS_POR_DEFECTO)
                .estado(EstadoCiclo.ABIERTO)
                .build();
        return cicloMapper.toDto(cicloRepository.save(ciclo));
    }

    /** Cierra el ciclo (cierre anticipado o al completar las 12 sesiones) y genera el GAS definitivo. */
    @Transactional
    public ProgresoCicloDTO cerrar(String cicloId, UsuarioAutenticado usuario) {
        Ciclo ciclo = cargarAbiertoParaEdicion(cicloId, usuario);
        cerrarInterno(ciclo, LocalDate.now());
        return progresoInterno(ciclo);
    }

    // ------------------------------------------------------------------ objetivos

    /** Activa en el ciclo un objetivo del banco. Responde 409 si ya hay 3 activos. */
    @Transactional
    public ObjetivoActivoDTO activarObjetivo(String cicloId, ActivarObjetivoRequest request, UsuarioAutenticado usuario) {
        Ciclo ciclo = cargarAbiertoParaEdicion(cicloId, usuario);
        BancoObjetivo banco = bancoObjetivoRepository.findById(request.bankId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Objetivo del banco", request.bankId()));
        if (!banco.getEspecialidad().getId().equals(ciclo.getEspecialidad().getId())) {
            throw new ReglaNegocioException("El objetivo %s pertenece a la especialidad %s, no a la del ciclo (%s)."
                    .formatted(banco.getId(), banco.getEspecialidad().getId(), ciclo.getEspecialidad().getId()));
        }
        if (objetivoCicloRepository.existsByCicloIdAndBancoObjetivoIdAndActivoTrue(cicloId, banco.getId())) {
            throw new ReglaNegocioException("Ese objetivo ya está activo en el ciclo.");
        }
        List<ObjetivoCiclo> activos = objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(cicloId);
        if (activos.size() >= ciclo.getMaxObjetivos()) {
            throw new ReglaNegocioException("Límite alcanzado: máximo %d objetivos activos por ciclo."
                    .formatted(ciclo.getMaxObjetivos()));
        }
        int lineaBase = request.baseline() != null ? request.baseline() : LINEA_BASE_POR_DEFECTO;
        int meta = request.target() != null ? request.target() : META_POR_DEFECTO;
        validarRangos(lineaBase, meta);

        ObjetivoCiclo objetivo = ObjetivoCiclo.builder()
                .id("obj-" + objetivoCicloRepository.siguienteSecuencia())
                .ciclo(ciclo)
                .bancoObjetivo(banco)
                .lineaBase(lineaBase)
                .meta(meta)
                .sesionInicio(ciclo.getSesionActual())
                .orden(primerOrdenLibre(activos, ciclo.getMaxObjetivos()))
                .activo(true)
                .build();
        return objetivoMapper.toDto(objetivoCicloRepository.save(objetivo));
    }

    /** Ajusta línea base y/o meta de un objetivo activo. */
    @Transactional
    public ObjetivoActivoDTO ajustarObjetivo(String cicloId, String objetivoId, AjustarObjetivoRequest request,
                                             UsuarioAutenticado usuario) {
        cargarAbiertoParaEdicion(cicloId, usuario);
        ObjetivoCiclo objetivo = cargarObjetivoActivo(cicloId, objetivoId);
        if (request.baseline() == null && request.target() == null) {
            throw new ValidacionException("Indica al menos la línea base (baseline) o la meta (target).");
        }
        int lineaBase = request.baseline() != null ? request.baseline() : objetivo.getLineaBase();
        int meta = request.target() != null ? request.target() : objetivo.getMeta();
        validarRangos(lineaBase, meta);
        objetivo.setLineaBase(lineaBase);
        objetivo.setMeta(meta);
        return objetivoMapper.toDto(objetivo);
    }

    /** Retira el objetivo del ciclo (deja de estar activo; se conserva el historial de evaluaciones). */
    @Transactional
    public void retirarObjetivo(String cicloId, String objetivoId, UsuarioAutenticado usuario) {
        cargarAbiertoParaEdicion(cicloId, usuario);
        cargarObjetivoActivo(cicloId, objetivoId).retirar();
    }

    // ------------------------------------------------------------------ sesiones

    /**
     * Guarda la sesión en curso con sus evaluaciones y textos. La sesión queda inmutable,
     * el ciclo avanza a la siguiente y, si era la última (12.ª), el ciclo se cierra y se genera el GAS.
     */
    @Transactional
    public SesionDTO registrarSesion(String cicloId, RegistrarSesionRequest request, UsuarioAutenticado usuario) {
        Ciclo ciclo = cargarAbiertoParaEdicion(cicloId, usuario);
        int numero = ciclo.getSesionActual();
        if (numero > ciclo.getTotalSesiones()) {
            throw new ReglaNegocioException("El ciclo ya completó sus %d sesiones.".formatted(ciclo.getTotalSesiones()));
        }
        if (request.number() != null && request.number() != numero) {
            throw new ReglaNegocioException("La sesión en curso del ciclo es la %d; no se puede registrar la sesión %d."
                    .formatted(numero, request.number()));
        }

        Map<String, ObjetivoCiclo> activos = objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(cicloId)
                .stream()
                .collect(Collectors.toMap(ObjetivoCiclo::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        if (activos.isEmpty()) {
            throw new ReglaNegocioException("El ciclo no tiene objetivos activos; activa al menos uno antes de registrar la sesión.");
        }
        List<String> desconocidos = request.evaluations().keySet().stream()
                .filter(id -> !activos.containsKey(id))
                .toList();
        if (!desconocidos.isEmpty()) {
            throw new ValidacionException("Las evaluaciones referencian objetivos que no están activos en el ciclo: "
                    + String.join(", ", desconocidos));
        }
        request.evaluations().forEach((id, ev) -> {
            if (ev.hits() > ev.total()) {
                throw new ValidacionException("Objetivo %s: los aciertos (%d) no pueden superar los intentos (%d)."
                        .formatted(id, ev.hits(), ev.total()));
            }
        });

        Profesional profesional = resolverProfesional(null, usuario, ciclo);
        Sesion sesion = Sesion.builder()
                .ciclo(ciclo)
                .numero(numero)
                .fecha(request.date() != null ? request.date() : LocalDate.now())
                .profesional(profesional)
                .actividad(request.activity().trim())
                .logro(request.achievement().trim())
                .observaciones(limpiar(request.observations()))
                .build();
        activos.forEach((id, objetivo) -> {
            EvaluacionRequest ev = request.evaluations().get(id);
            if (ev != null) {
                sesion.agregarEvaluacion(objetivo, ev.hits(), ev.total());
            }
        });
        Sesion guardada = sesionRepository.save(sesion);

        ciclo.setSesionActual(numero + 1);
        if (numero >= ciclo.getTotalSesiones()) {
            cerrarInterno(ciclo, guardada.getFecha());
            log.info("Ciclo {} cerrado al registrar la sesión {}", ciclo.getId(), numero);
        }
        return sesionMapper.toDto(guardada);
    }

    // ------------------------------------------------------------------ internos

    private Ciclo cargar(String cicloId) {
        return cicloRepository.findDetalladoById(cicloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ciclo", cicloId));
    }

    private Ciclo cargarAbiertoParaEdicion(String cicloId, UsuarioAutenticado usuario) {
        Ciclo ciclo = cargar(cicloId);
        autorizacionService.verificarPuedeEditarEspecialidad(ciclo.getEspecialidad().getId(), usuario);
        if (!ciclo.estaAbierto()) {
            throw new ReglaNegocioException("El ciclo %s está cerrado; no admite cambios.".formatted(cicloId));
        }
        return ciclo;
    }

    private ObjetivoCiclo cargarObjetivoActivo(String cicloId, String objetivoId) {
        ObjetivoCiclo objetivo = objetivoCicloRepository.findByIdAndCicloId(objetivoId, cicloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Objetivo del ciclo", objetivoId));
        if (!objetivo.isActivo()) {
            throw new ReglaNegocioException("El objetivo %s ya fue retirado del ciclo.".formatted(objetivoId));
        }
        return objetivo;
    }

    /** Profesional que firma la operación: el del usuario; si no tiene (coordinación), el indicado o el responsable del ciclo. */
    private Profesional resolverProfesional(String profesionalId, UsuarioAutenticado usuario, Ciclo ciclo) {
        if (usuario.tieneProfesional()) {
            return profesionalRepository.findById(usuario.getProfesionalId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", usuario.getProfesionalId()));
        }
        if (profesionalId != null && !profesionalId.isBlank()) {
            return profesionalRepository.findById(profesionalId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", profesionalId));
        }
        if (ciclo != null) {
            return ciclo.getProfesional();
        }
        throw new ValidacionException("El usuario no tiene un profesional asociado; indica professionalId.");
    }

    private void cerrarInterno(Ciclo ciclo, LocalDate fecha) {
        ciclo.setEstado(EstadoCiclo.CERRADO);
        ciclo.setFechaCierre(fecha);
        List<SesionRegistrada> sesiones = SesionesDominio.aDominio(sesionRepository.findByCicloIdOrderByNumeroAsc(ciclo.getId()));
        for (ObjetivoCiclo objetivo : objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(ciclo.getId())) {
            Integer ultimo = progresoService.ultimoPuntaje(sesiones, objetivo.getId());
            NivelGas nivel = progresoService.calcularGas(ultimo, objetivo.getLineaBase(), objetivo.getMeta());
            if (nivel == null || resultadoGasRepository.findByObjetivoCicloId(objetivo.getId()).isPresent()) {
                continue; // objetivo nunca evaluado o GAS ya generado
            }
            resultadoGasRepository.save(ResultadoGas.builder()
                    .objetivoCiclo(objetivo)
                    .nivel(nivel.getNivel())
                    .puntaje(ultimo)
                    .decision(nivel.getDecision())
                    .fecha(fecha)
                    .build());
        }
    }

    private ProgresoCicloDTO progresoInterno(Ciclo ciclo) {
        List<SesionRegistrada> sesiones = SesionesDominio.aDominio(sesionRepository.findByCicloIdOrderByNumeroAsc(ciclo.getId()));
        Map<String, ResultadoGas> finales = resultadoGasRepository.findByObjetivoCicloCicloId(ciclo.getId()).stream()
                .collect(Collectors.toMap(r -> r.getObjetivoCiclo().getId(), Function.identity()));

        List<ProgresoObjetivoDTO> objetivos = objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(ciclo.getId())
                .stream()
                .map(o -> {
                    Integer ultimo = progresoService.ultimoPuntaje(sesiones, o.getId());
                    NivelGas gas = progresoService.calcularGas(ultimo, o.getLineaBase(), o.getMeta());
                    List<PuntoSerieDTO> serie = progresoService.serie(sesiones, o.getId()).stream()
                            .map(p -> new PuntoSerieDTO(p.session(), p.date(), p.value()))
                            .toList();
                    return new ProgresoObjetivoDTO(
                            o.getId(),
                            o.getBancoObjetivo().getId(),
                            o.getBancoObjetivo().getTexto(),
                            o.getLineaBase(),
                            o.getMeta(),
                            progresoService.progresoAcumulado(sesiones, o.getId()),
                            ultimo,
                            progresoService.ultimaVariacion(sesiones, o.getId()),
                            progresoService.sesionesEvaluadas(sesiones, o.getId()),
                            serie,
                            GasDTO.de(gas),
                            ResultadoGasDTO.de(finales.get(o.getId())));
                })
                .toList();

        return new ProgresoCicloDTO(
                ciclo.getId(),
                ciclo.getEspecialidad().getId(),
                ciclo.getSesionActual(),
                ciclo.getTotalSesiones(),
                ciclo.getEstado().toApi(),
                progresoService.cicloEnCierre(ciclo.getEstado(), ciclo.getSesionActual(), ciclo.getTotalSesiones()),
                objetivos);
    }

    private static void validarRangos(int lineaBase, int meta) {
        if (lineaBase < 0 || lineaBase > 100 || meta < 0 || meta > 100) {
            throw new ValidacionException("La línea base y la meta deben estar entre 0 y 100.");
        }
        if (meta <= lineaBase) {
            throw new ValidacionException("La meta (target) debe ser mayor que la línea base (baseline).");
        }
    }

    private static int primerOrdenLibre(List<ObjetivoCiclo> activos, int maxObjetivos) {
        for (int orden = 1; orden <= maxObjetivos; orden++) {
            int candidato = orden;
            if (activos.stream().noneMatch(o -> o.getOrden() == candidato)) {
                return orden;
            }
        }
        throw new ReglaNegocioException("Límite alcanzado: máximo %d objetivos activos por ciclo.".formatted(maxObjetivos));
    }

    private static String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
