package pe.sanjuandedios.clinica.dto;

/** Elemento del banco de objetivos. Misma forma que {@code objectiveBank[TO][i]} en mockData.js más {@code specialtyId}. */
public record BancoObjetivoDTO(String id, String specialtyId, String text) {}
