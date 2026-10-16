package pe.sanjuandedios.clinica.domain.progreso;

import java.time.LocalDate;

/** Punto de la serie de % por sesión de un objetivo ({@code value} es {@code null} cuando no se evaluó). */
public record PuntoSerie(int session, LocalDate date, Integer value) {}
