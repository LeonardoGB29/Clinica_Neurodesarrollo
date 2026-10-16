package pe.sanjuandedios.clinica.domain.progreso;

import java.time.LocalDate;
import java.util.Map;

/**
 * Vista de dominio de una sesión registrada, independiente de JPA, para calcular el progreso.
 * {@code evaluaciones} está indexado por el id del objetivo del ciclo.
 */
public record SesionRegistrada(int numero, LocalDate fecha, Map<String, EvaluacionRegistrada> evaluaciones) {

    /** Evaluación del objetivo en esta sesión o {@code null} si no fue evaluado. */
    public EvaluacionRegistrada evaluacionDe(String objetivoId) {
        return evaluaciones == null ? null : evaluaciones.get(objetivoId);
    }
}
