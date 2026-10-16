package pe.sanjuandedios.clinica.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.NotaCompartida;
import pe.sanjuandedios.clinica.dto.NotaCompartidaDTO;

@Mapper
public interface NotaMapper {

    @Mapping(target = "specialtyId", source = "especialidad.id")
    @Mapping(target = "professionalName", source = "profesional.nombres")
    @Mapping(target = "date", source = "fecha")
    @Mapping(target = "text", source = "texto")
    NotaCompartidaDTO toDto(NotaCompartida nota);

    List<NotaCompartidaDTO> toDtoList(List<NotaCompartida> notas);
}
