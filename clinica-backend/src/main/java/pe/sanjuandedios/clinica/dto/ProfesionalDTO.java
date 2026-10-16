package pe.sanjuandedios.clinica.dto;

/** Misma forma que {@code currentProfessional} en mockData.js. */
public record ProfesionalDTO(
        String id,
        String fullName,
        String initials,
        String specialtyId,
        String college,
        String licenseNumber,
        String email) {}
