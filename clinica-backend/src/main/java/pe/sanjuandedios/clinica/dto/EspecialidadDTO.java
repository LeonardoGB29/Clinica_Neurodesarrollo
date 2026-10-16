package pe.sanjuandedios.clinica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Misma forma que {@code SPECIALTIES[id]} en mockData.js: {@code { id, name, short, college, color }}. */
public record EspecialidadDTO(
        String id,
        String name,
        @JsonProperty("short") String shortName,
        String college,
        String color) {}
