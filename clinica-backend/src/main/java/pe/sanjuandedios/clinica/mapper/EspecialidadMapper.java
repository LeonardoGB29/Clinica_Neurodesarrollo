package pe.sanjuandedios.clinica.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.Especialidad;
import pe.sanjuandedios.clinica.dto.EspecialidadDTO;

@Mapper
public interface EspecialidadMapper {

    @Mapping(target = "name", source = "nombre")
    @Mapping(target = "shortName", source = "nombreCorto")
    @Mapping(target = "college", source = "colegioProfesional")
    EspecialidadDTO toDto(Especialidad especialidad);

    List<EspecialidadDTO> toDtoList(List<Especialidad> especialidades);
}
