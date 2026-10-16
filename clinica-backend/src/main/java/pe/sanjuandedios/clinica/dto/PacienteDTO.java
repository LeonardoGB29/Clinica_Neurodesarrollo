package pe.sanjuandedios.clinica.dto;

import java.time.LocalDate;
import java.util.List;

/** Misma forma que {@code patient} en mockData.js. */
public record PacienteDTO(
        String id,
        String historyNumber,
        String fullName,
        String initials,
        LocalDate birthDate,
        String sex,
        List<String> diagnoses,
        ApoderadoDTO guardian,
        String coverage,
        String school) {

    /** Apoderado del paciente: {@code { name, relation, phone }}. */
    public record ApoderadoDTO(String name, String relation, String phone) {}
}
