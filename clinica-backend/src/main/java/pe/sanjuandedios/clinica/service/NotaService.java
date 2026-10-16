package pe.sanjuandedios.clinica.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.domain.NotaCompartida;
import pe.sanjuandedios.clinica.domain.Paciente;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.dto.NotaCompartidaDTO;
import pe.sanjuandedios.clinica.dto.Requests.CrearNotaRequest;
import pe.sanjuandedios.clinica.mapper.NotaMapper;
import pe.sanjuandedios.clinica.repository.NotaCompartidaRepository;
import pe.sanjuandedios.clinica.repository.PacienteRepository;
import pe.sanjuandedios.clinica.repository.ProfesionalRepository;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;
import pe.sanjuandedios.clinica.web.error.ValidacionException;

/** Notas interdisciplinarias compartidas (equivalen a {@code sharedNotes} del prototipo). */
@Service
@RequiredArgsConstructor
public class NotaService {

    private final NotaCompartidaRepository notaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AutorizacionService autorizacionService;
    private final NotaMapper notaMapper;

    @Transactional(readOnly = true)
    public List<NotaCompartidaDTO> listar(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RecursoNoEncontradoException("Paciente", pacienteId);
        }
        return notaMapper.toDtoList(notaRepository.findByPacienteIdOrderByFechaDescIdDesc(pacienteId));
    }

    /** Publica una nota a nombre del profesional logueado. */
    @Transactional
    public NotaCompartidaDTO crear(String pacienteId, CrearNotaRequest request, UsuarioAutenticado usuario) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", pacienteId));
        if (!usuario.tieneProfesional()) {
            throw new ValidacionException("Solo los profesionales con especialidad pueden publicar notas compartidas.");
        }
        Profesional autor = profesionalRepository.findById(usuario.getProfesionalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", usuario.getProfesionalId()));
        autorizacionService.verificarPuedeEditarEspecialidad(autor.getEspecialidad().getId(), usuario);

        NotaCompartida nota = NotaCompartida.builder()
                .id("note-" + notaRepository.siguienteSecuencia())
                .paciente(paciente)
                .especialidad(autor.getEspecialidad())
                .profesional(autor)
                .fecha(request.date() != null ? request.date() : LocalDate.now())
                .texto(request.text().trim())
                .build();
        return notaMapper.toDto(notaRepository.save(nota));
    }
}
