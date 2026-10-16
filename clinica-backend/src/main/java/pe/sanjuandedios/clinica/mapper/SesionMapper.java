package pe.sanjuandedios.clinica.mapper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.EvaluacionObjetivo;
import pe.sanjuandedios.clinica.domain.Sesion;
import pe.sanjuandedios.clinica.dto.SesionDTO;

@Mapper
public interface SesionMapper {

    @Mapping(target = "number", source = "numero")
    @Mapping(target = "date", source = "fecha")
    @Mapping(target = "professionalId", source = "profesional.id")
    @Mapping(target = "professionalName", source = "profesional.nombres")
    @Mapping(target = "evaluations", source = "evaluaciones")
    @Mapping(target = "activity", source = "actividad")
    @Mapping(target = "achievement", source = "logro")
    @Mapping(target = "observations", source = "observaciones")
    SesionDTO toDto(Sesion sesion);

    List<SesionDTO> toDtoList(List<Sesion> sesiones);

    /** {@code evaluations[objectiveId] = { hits, total }}, en el orden de los objetivos del ciclo. */
    default Map<String, SesionDTO.EvaluacionDTO> toEvaluaciones(List<EvaluacionObjetivo> evaluaciones) {
        if (evaluaciones == null) {
            return new LinkedHashMap<>();
        }
        return evaluaciones.stream()
                .sorted(Comparator.comparingInt(e -> e.getObjetivoCiclo().getOrden()))
                .collect(Collectors.toMap(
                        e -> e.getObjetivoCiclo().getId(),
                        e -> new SesionDTO.EvaluacionDTO(e.getAciertos(), e.getIntentos()),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
