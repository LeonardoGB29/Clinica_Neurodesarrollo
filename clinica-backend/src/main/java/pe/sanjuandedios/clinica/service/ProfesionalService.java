package pe.sanjuandedios.clinica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.dto.ProfesionalDTO;
import pe.sanjuandedios.clinica.mapper.ProfesionalMapper;
import pe.sanjuandedios.clinica.repository.ProfesionalRepository;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;

@Service
@RequiredArgsConstructor
public class ProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final ProfesionalMapper profesionalMapper;

    /** Datos del profesional logueado (cabecera de la ficha). */
    @Transactional(readOnly = true)
    public ProfesionalDTO me(UsuarioAutenticado usuario) {
        if (!usuario.tieneProfesional()) {
            throw new RecursoNoEncontradoException(
                    "El usuario '%s' no tiene un profesional asociado.".formatted(usuario.getUsername()));
        }
        return profesionalRepository.findById(usuario.getProfesionalId())
                .map(profesionalMapper::toDto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", usuario.getProfesionalId()));
    }
}
