package pe.sanjuandedios.clinica.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Sesión registrada (solo lectura). Misma forma que los elementos de {@code sessionHistory} en mockData.js:
 * {@code evaluations[objectiveId] = { hits, total }}. {@code id} y {@code professionalId} son adicionales.
 */
public record SesionDTO(
        Long id,
        int number,
        LocalDate date,
        String professionalId,
        String professionalName,
        Map<String, EvaluacionDTO> evaluations,
        String activity,
        String achievement,
        String observations) {

    /** Aciertos sobre intentos de un objetivo en la sesión. */
    public record EvaluacionDTO(int hits, int total) {}
}
