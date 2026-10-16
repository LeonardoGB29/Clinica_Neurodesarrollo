package pe.sanjuandedios.clinica.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.domain.Ciclo;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.domain.Sesion;
import pe.sanjuandedios.clinica.domain.progreso.ProgresoService;
import pe.sanjuandedios.clinica.domain.progreso.PuntoSerie;
import pe.sanjuandedios.clinica.domain.progreso.SesionRegistrada;
import pe.sanjuandedios.clinica.dto.Vista360DTO;
import pe.sanjuandedios.clinica.dto.Vista360DTO.ObjetivoVista360DTO;
import pe.sanjuandedios.clinica.repository.CicloRepository;
import pe.sanjuandedios.clinica.repository.PacienteRepository;
import pe.sanjuandedios.clinica.repository.SesionRepository;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;

/**
 * Vista 360° interdisciplinaria: objetivos y progreso del paciente en las demás especialidades
 * (se excluye la especialidad del profesional logueado; coordinación y sistemas ven todas).
 */
@Service
@RequiredArgsConstructor
public class Vista360Service {

    private final PacienteRepository pacienteRepository;
    private final CicloRepository cicloRepository;
    private final SesionRepository sesionRepository;
    private final ProgresoService progresoService;

    @Transactional(readOnly = true)
    public List<Vista360DTO> obtener(String pacienteId, UsuarioAutenticado usuario) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RecursoNoEncontradoException("Paciente", pacienteId);
        }
        String propia = usuario.getEspecialidadId();
        return cicloRepository.findByPacienteIdAndEstado(pacienteId, EstadoCiclo.ABIERTO).stream()
                .filter(c -> propia == null || !propia.equalsIgnoreCase(c.getEspecialidad().getId()))
                .sorted(Comparator.comparingInt(c -> c.getEspecialidad().getOrden()))
                .map(this::resumir)
                .toList();
    }

    private Vista360DTO resumir(Ciclo ciclo) {
        List<Sesion> sesiones = sesionRepository.findByCicloIdOrderByNumeroAsc(ciclo.getId());
        List<SesionRegistrada> dominio = SesionesDominio.aDominio(sesiones);
        Sesion ultima = sesiones.isEmpty() ? null : sesiones.get(sesiones.size() - 1);

        List<ObjetivoVista360DTO> objetivos = ciclo.getObjetivosActivos().stream()
                .map(o -> new ObjetivoVista360DTO(
                        o.getId(),
                        o.getBancoObjetivo().getTexto(),
                        o.getLineaBase(),
                        o.getMeta(),
                        progresoService.serie(dominio, o.getId()).stream()
                                .map(PuntoSerie::value)
                                .filter(Objects::nonNull)
                                .toList()))
                .toList();

        Profesional profesional = ciclo.getProfesional();
        return new Vista360DTO(
                ciclo.getEspecialidad().getId(),
                ciclo.getId(),
                profesional.getNombres(),
                profesional.getLicencia(),
                ultima == null ? 0 : ultima.getNumero(),
                ciclo.getTotalSesiones(),
                ultima == null ? null : ultima.getFecha(),
                objetivos,
                ultima == null ? null : ultima.getObservaciones());
    }
}
