package pe.sanjuandedios.clinica.dto;

import java.time.LocalDate;

/** Nota interdisciplinaria. Misma forma que {@code sharedNotes[i]} en mockData.js más {@code professionalName}. */
public record NotaCompartidaDTO(String id, String specialtyId, String professionalName, LocalDate date, String text) {}
