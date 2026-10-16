package pe.sanjuandedios.clinica.domain.progreso;

import java.util.Arrays;

/**
 * Escala GAS (Goal Attainment Scaling):
 * <pre>
 *   +2  Mucho mejor de lo esperado  → subir carga / nuevo objetivo
 *   +1  Mejor de lo esperado        → subir carga
 *    0  Resultado esperado (meta)   → mantener
 *   -1  Menor de lo esperado        → mantener con ajustes
 *   -2  Sin cambios / retroceso     → bajar carga
 * </pre>
 * Equivale a {@code GAS_LEVELS} de {@code progress.js} en el front.
 */
public enum NivelGas {
    SIN_CAMBIOS(-2, "Sin cambios", "Bajar carga", "danger"),
    MENOR_A_LO_ESPERADO(-1, "Menor a lo esperado", "Mantener con ajustes", "warning"),
    RESULTADO_ESPERADO(0, "Resultado esperado", "Mantener", "neutral"),
    MEJOR_A_LO_ESPERADO(1, "Mejor a lo esperado", "Subir carga", "success"),
    MUCHO_MEJOR(2, "Mucho mejor", "Subir carga / nuevo objetivo", "success");

    private final int nivel;
    private final String etiqueta;
    private final String decision;
    private final String tono;

    NivelGas(int nivel, String etiqueta, String decision, String tono) {
        this.nivel = nivel;
        this.etiqueta = etiqueta;
        this.decision = decision;
        this.tono = tono;
    }

    public int getNivel() {
        return nivel;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDecision() {
        return decision;
    }

    /** Tono semántico para la UI: danger, warning, neutral, success. */
    public String getTono() {
        return tono;
    }

    public static NivelGas porNivel(int nivel) {
        return Arrays.stream(values())
                .filter(g -> g.nivel == nivel)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nivel GAS fuera de rango (-2…+2): " + nivel));
    }
}
