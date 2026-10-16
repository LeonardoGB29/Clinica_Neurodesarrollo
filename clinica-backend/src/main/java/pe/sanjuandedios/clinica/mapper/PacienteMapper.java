package pe.sanjuandedios.clinica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.Paciente;
import pe.sanjuandedios.clinica.dto.PacienteDTO;

@Mapper
public interface PacienteMapper {

    @Mapping(target = "historyNumber", source = "nroHistoria")
    @Mapping(target = "fullName", source = "nombres")
    @Mapping(target = "initials", source = "iniciales")
    @Mapping(target = "birthDate", source = "fechaNac")
    @Mapping(target = "sex", source = "sexo")
    @Mapping(target = "diagnoses", source = "diagnosticos")
    @Mapping(target = "guardian", source = ".")
    @Mapping(target = "coverage", source = "cobertura")
    @Mapping(target = "school", source = "colegio")
    PacienteDTO toDto(Paciente paciente);

    @Mapping(target = "name", source = "apoderadoNombre")
    @Mapping(target = "relation", source = "apoderadoRelacion")
    @Mapping(target = "phone", source = "apoderadoTelefono")
    PacienteDTO.ApoderadoDTO toApoderado(Paciente paciente);
}
