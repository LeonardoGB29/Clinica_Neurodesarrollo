package pe.sanjuandedios.clinica.dto;

import java.time.LocalDate;
import java.util.List;
import pe.sanjuandedios.clinica.domain.ResultadoGas;
import pe.sanjuandedios.clinica.domain.progreso.NivelGas;

/**
 * Progreso del ciclo (GET /ciclos/{id}/progreso): por objetivo, % acumulado, última sesión,
 * variación, serie por sesión y nivel GAS. Los nombres siguen a las funciones de progress.js
 * ({@code accumulatedProgress}, {@code lastScore}, {@code lastDelta}, {@code recordedCount}, {@code scoreSeries}, {@code computeGas}).
 */
public record ProgresoCicloDTO(
        String cycleId,
        String specialtyId,
        int currentSession,
        int totalSessions,
        String status,
        boolean closing,
        List<ProgresoObjetivoDTO> objectives) {

    public record ProgresoObjetivoDTO(
            String id,
            String bankId,
            String text,
            int baseline,
            int target,
            int accumulated,
            Integer lastScore,
            Integer lastDelta,
            int recordedCount,
            List<PuntoSerieDTO> series,
            GasDTO gas,
            ResultadoGasDTO gasFinal) {}

    /** % de una sesión ({@code value} = null cuando el objetivo no se evaluó). */
    public record PuntoSerieDTO(int session, LocalDate date, Integer value) {}

    /** Nivel GAS calculado en vivo con el último % registrado. Misma forma que {@code GAS_LEVELS[i]} en progress.js. */
    public record GasDTO(int level, String label, String decision, String tone) {

        public static GasDTO de(NivelGas nivel) {
            return nivel == null ? null : new GasDTO(nivel.getNivel(), nivel.getEtiqueta(), nivel.getDecision(), nivel.getTono());
        }
    }

    /** Resultado GAS definitivo persistido al cerrar el ciclo. */
    public record ResultadoGasDTO(int level, int score, String label, String decision, String tone, LocalDate date) {

        public static ResultadoGasDTO de(ResultadoGas resultado) {
            if (resultado == null) {
                return null;
            }
            NivelGas nivel = NivelGas.porNivel(resultado.getNivel());
            return new ResultadoGasDTO(nivel.getNivel(), resultado.getPuntaje(), nivel.getEtiqueta(),
                    resultado.getDecision(), nivel.getTono(), resultado.getFecha());
        }
    }
}
