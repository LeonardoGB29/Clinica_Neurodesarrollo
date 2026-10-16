package pe.sanjuandedios.clinica.domain.progreso;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;

/**
 * Reglas de cálculo de progreso y escala GAS.
 *
 * <p>Es el porte 1:1 de {@code clinica-frontend/src/utils/progress.js}: el front solo muestra
 * lo que esta clase calcula. Las fórmulas se mantienen idénticas (incluido el redondeo con
 * {@code Math.round}) para que ambos extremos coincidan mientras convive el prototipo.</p>
 *
 * <ul>
 *   <li>% de sesión = aciertos / intentos (acotado a 100).</li>
 *   <li>% acumulado del objetivo = promedio de las sesiones en que fue evaluado.</li>
 *   <li>GAS: score ≥ meta+15 → +2 · score ≥ meta+5 → +1 · score ≥ meta−10 → 0 ·
 *       score &gt; línea base → −1 · score ≤ línea base → −2.</li>
 * </ul>
 */
@Service
public class ProgresoService {

    /** % de éxito de una evaluación: aciertos / intentos (ej. 3 de 10 = 30 %). {@code null} si no es calculable. */
    public Integer tasaExito(Integer aciertos, Integer intentos) {
        if (aciertos == null || intentos == null) {
            return null;
        }
        if (intentos <= 0 || aciertos < 0) {
            return null;
        }
        double proporcion = Math.min((double) aciertos / intentos, 1.0);
        return (int) Math.round(proporcion * 100);
    }

    /** % de éxito de una evaluación; {@code null} si no hubo evaluación. */
    public Integer tasaExito(EvaluacionRegistrada evaluacion) {
        if (evaluacion == null) {
            return null;
        }
        return tasaExito(evaluacion.aciertos(), evaluacion.intentos());
    }

    /** Serie de % por sesión para un objetivo ({@code value} = {@code null} cuando no se evaluó). */
    public List<PuntoSerie> serie(List<SesionRegistrada> sesiones, String objetivoId) {
        return sesiones.stream()
                .map(s -> new PuntoSerie(s.numero(), s.fecha(), tasaExito(s.evaluacionDe(objetivoId))))
                .toList();
    }

    private List<Integer> valoresRegistrados(List<SesionRegistrada> sesiones, String objetivoId) {
        return serie(sesiones, objetivoId).stream()
                .map(PuntoSerie::value)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Último % registrado (nivel actual del objetivo) o {@code null} si nunca se evaluó. */
    public Integer ultimoPuntaje(List<SesionRegistrada> sesiones, String objetivoId) {
        List<Integer> valores = valoresRegistrados(sesiones, objetivoId);
        return valores.isEmpty() ? null : valores.get(valores.size() - 1);
    }

    /**
     * % de avance acumulado del objetivo en el ciclo.
     * Regla vigente: promedio de la tasa de éxito de las sesiones registradas (0 si no hay ninguna).
     */
    public int progresoAcumulado(List<SesionRegistrada> sesiones, String objetivoId) {
        List<Integer> valores = valoresRegistrados(sesiones, objetivoId);
        if (valores.isEmpty()) {
            return 0;
        }
        double suma = valores.stream().mapToInt(Integer::intValue).sum();
        return (int) Math.round(suma / valores.size());
    }

    /** Variación entre las dos últimas sesiones registradas (puntos porcentuales) o {@code null} si hay menos de dos. */
    public Integer ultimaVariacion(List<SesionRegistrada> sesiones, String objetivoId) {
        List<Integer> valores = valoresRegistrados(sesiones, objetivoId);
        if (valores.size() < 2) {
            return null;
        }
        return valores.get(valores.size() - 1) - valores.get(valores.size() - 2);
    }

    /** Cantidad de sesiones en las que el objetivo fue evaluado. */
    public int sesionesEvaluadas(List<SesionRegistrada> sesiones, String objetivoId) {
        return valoresRegistrados(sesiones, objetivoId).size();
    }

    /**
     * Nivel GAS a partir del % alcanzado, la línea base y la meta.
     * Bandas (ajustables con el equipo clínico):
     * score ≥ meta+15 → +2 | score ≥ meta+5 → +1 | score ≥ meta−10 → 0 | score &gt; línea base → −1 | resto → −2.
     *
     * @return el nivel o {@code null} si no hay puntaje.
     */
    public NivelGas calcularGas(Integer puntaje, int lineaBase, int meta) {
        if (puntaje == null) {
            return null;
        }
        if (puntaje >= meta + 15) {
            return NivelGas.MUCHO_MEJOR;
        }
        if (puntaje >= meta + 5) {
            return NivelGas.MEJOR_A_LO_ESPERADO;
        }
        if (puntaje >= meta - 10) {
            return NivelGas.RESULTADO_ESPERADO;
        }
        if (puntaje > lineaBase) {
            return NivelGas.MENOR_A_LO_ESPERADO;
        }
        return NivelGas.SIN_CAMBIOS;
    }

    /** El GAS definitivo se habilita en la última sesión del ciclo (semana 12) o cuando el ciclo ya está cerrado. */
    public boolean cicloEnCierre(EstadoCiclo estado, int sesionActual, int totalSesiones) {
        return estado == EstadoCiclo.CERRADO || sesionActual >= totalSesiones;
    }
}
