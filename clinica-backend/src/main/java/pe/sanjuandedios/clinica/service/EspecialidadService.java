package pe.sanjuandedios.clinica.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.dto.EspecialidadDTO;
import pe.sanjuandedios.clinica.mapper.EspecialidadMapper;
import pe.sanjuandedios.clinica.repository.EspecialidadRepository;

@Service
@RequiredArgsConstructor
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final EspecialidadMapper especialidadMapper;

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listar() {
        return especialidadMapper.toDtoList(especialidadRepository.findAllByOrderByOrdenAsc());
    }
}
