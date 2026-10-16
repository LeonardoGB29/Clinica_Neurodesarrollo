package pe.sanjuandedios.clinica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.Ciclo;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;
import pe.sanjuandedios.clinica.dto.CicloDTO;

@Mapper(uses = ObjetivoMapper.class)
public interface CicloMapper {

    @Mapping(target = "patientId", source = "paciente.id")
    @Mapping(target = "specialtyId", source = "especialidad.id")
    @Mapping(target = "professionalId", source = "profesional.id")
    @Mapping(target = "startDate", source = "fechaInicio")
    @Mapping(target = "totalSessions", source = "totalSesiones")
    @Mapping(target = "currentSession", source = "sesionActual")
    @Mapping(target = "frequency", source = "frecuencia")
    @Mapping(target = "maxObjectives", source = "maxObjetivos")
    @Mapping(target = "status", source = "estado")
    @Mapping(target = "closedAt", source = "fechaCierre")
    @Mapping(target = "objectives", source = "objetivosActivos")
    CicloDTO toDto(Ciclo ciclo);

    /** "open" / "closed", como espera el front. */
    default String estadoToApi(EstadoCiclo estado) {
        return estado == null ? null : estado.toApi();
    }
}
