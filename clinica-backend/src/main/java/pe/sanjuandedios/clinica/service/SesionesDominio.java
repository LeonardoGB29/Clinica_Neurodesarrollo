package pe.sanjuandedios.clinica.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pe.sanjuandedios.clinica.domain.EvaluacionObjetivo;
import pe.sanjuandedios.clinica.domain.Sesion;
import pe.sanjuandedios.clinica.domain.progreso.EvaluacionRegistrada;
import pe.sanjuandedios.clinica.domain.progreso.SesionRegistrada;

/** Convierte las sesiones persistidas (JPA) a la vista de dominio que usa {@code ProgresoService}. */
public final class SesionesDominio {

    private SesionesDominio() {}

    public static List<SesionRegistrada> aDominio(List<Sesion> sesiones) {
        return sesiones.stream()
                .map(s -> new SesionRegistrada(s.getNumero(), s.getFecha(), evaluaciones(s.getEvaluaciones())))
                .toList();
    }

    private static Map<String, EvaluacionRegistrada> evaluaciones(List<EvaluacionObjetivo> evaluaciones) {
        return evaluaciones.stream().collect(Collectors.toMap(
                e -> e.getObjetivoCiclo().getId(),
                e -> new EvaluacionRegistrada(e.getAciertos(), e.getIntentos()),
                (a, b) -> a,
                LinkedHashMap::new));
    }
}
