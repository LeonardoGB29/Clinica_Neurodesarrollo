package pe.sanjuandedios.clinica.dto;

/**
 * Objetivo activo del ciclo. Misma forma que los elementos de {@code activeObjectives} en mockData.js
 * ({@code order} es adicional: posición 1..3 dentro del ciclo).
 */
public record ObjetivoActivoDTO(
        String id,
        String bankId,
        String text,
        int baseline,
        int target,
        int addedInSession,
        int order) {}
