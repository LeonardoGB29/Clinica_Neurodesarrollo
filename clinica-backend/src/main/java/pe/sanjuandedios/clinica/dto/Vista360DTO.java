package pe.sanjuandedios.clinica.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Vista 360° interdisciplinaria: objetivos y progreso del paciente en otra especialidad.
 * Misma forma que los elementos de {@code otherSpecialtiesProgress} en mockData.js
 * ({@code cycleId} y {@code baseline} son adicionales).
 * <ul>
 *   <li>{@code currentSession}: número de la última sesión registrada.</li>
 *   <li>{@code history}: % de éxito por sesión evaluada (para el sparkline).</li>
 *   <li>{@code lastNote}: observaciones de la última sesión registrada.</li>
 * </ul>
 */
public record Vista360DTO(
        String specialtyId,
        String cycleId,
        String professionalName,
        String license,
        int currentSession,
        int totalSessions,
        LocalDate lastSessionDate,
        List<ObjetivoVista360DTO> objectives,
        String lastNote) {

    public record ObjetivoVista360DTO(String id, String text, int baseline, int target, List<Integer> history) {}
}
