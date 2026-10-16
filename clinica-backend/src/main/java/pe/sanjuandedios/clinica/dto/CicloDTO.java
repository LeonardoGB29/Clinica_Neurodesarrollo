package pe.sanjuandedios.clinica.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Ciclo terapéutico. Misma forma que {@code cycle} en mockData.js más {@code objectives}
 * (los objetivos activos, equivalentes a {@code activeObjectives}) y datos de contexto
 * ({@code patientId}, {@code professionalId}, {@code closedAt}).
 */
public record CicloDTO(
        String id,
        String patientId,
        String specialtyId,
        String professionalId,
        LocalDate startDate,
        int totalSessions,
        int currentSession,
        String frequency,
        int maxObjectives,
        String status,
        LocalDate closedAt,
        List<ObjetivoActivoDTO> objectives) {}
