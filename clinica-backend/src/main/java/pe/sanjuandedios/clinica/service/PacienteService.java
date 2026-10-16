package pe.sanjuandedios.clinica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.dto.PacienteDTO;
import pe.sanjuandedios.clinica.mapper.PacienteMapper;
import pe.sanjuandedios.clinica.repository.PacienteRepository;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    @Transactional(readOnly = true)
    public PacienteDTO obtener(String id) {
        return pacienteRepository.findById(id)
                .map(pacienteMapper::toDto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", id));
    }
}
