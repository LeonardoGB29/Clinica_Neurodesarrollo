package pe.sanjuandedios.clinica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.dto.ProfesionalDTO;

@Mapper
public interface ProfesionalMapper {

    @Mapping(target = "fullName", source = "nombres")
    @Mapping(target = "initials", source = "iniciales")
    @Mapping(target = "specialtyId", source = "especialidad.id")
    @Mapping(target = "college", source = "colegio")
    @Mapping(target = "licenseNumber", source = "nroColegiatura")
    @Mapping(target = "email", source = "correo")
    ProfesionalDTO toDto(Profesional profesional);
}
