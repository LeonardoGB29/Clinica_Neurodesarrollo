package pe.sanjuandedios.clinica.domain.progreso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;

/**
 * Pruebas del porte de {@code progress.js}. Los casos del historial reproducen
 * {@code sessionHistory} de mockData.js para garantizar que back y front calculan lo mismo.
 */
class ProgresoServiceTest {

    private final ProgresoService servicio = new ProgresoService();

    /** sessionHistory del prototipo: obj-1 → 2/10, 3/10, 5/10 · obj-2 → 1/5, 2/6, 3/6. */
    private static final List<SesionRegistrada> HISTORIAL_TO = List.of(
            sesion(1, "2026-09-24", Map.of("obj-1", ev(2, 10), "obj-2", ev(1, 5))),
            sesion(2, "2026-10-01", Map.of("obj-1", ev(3, 10), "obj-2", ev(2, 6))),
            sesion(3, "2026-10-08", Map.of("obj-1", ev(5, 10), "obj-2", ev(3, 6))));

    private static SesionRegistrada sesion(int numero, String fecha, Map<String, EvaluacionRegistrada> evaluaciones) {
        return new SesionRegistrada(numero, LocalDate.parse(fecha), evaluaciones);
    }

    private static EvaluacionRegistrada ev(int aciertos, int intentos) {
        return new EvaluacionRegistrada(aciertos, intentos);
    }

    @Nested
    @DisplayName("tasaExito: % = aciertos / intentos")
    class TasaExito {

        @ParameterizedTest(name = "{0} de {1} = {2} %")
        @CsvSource({
            "3, 10, 30",
            "11, 20, 55",
            "1, 3, 33",
            "2, 3, 67",
            "1, 8, 13",
            "0, 10, 0",
            "10, 10, 100",
            "12, 10, 100"
        })
        void calculaYRedondeaComoElFront(int aciertos, int intentos, int esperado) {
            assertThat(servicio.tasaExito(aciertos, intentos)).isEqualTo(esperado);
        }

        @Test
        void devuelveNullCuandoNoEsCalculable() {
            assertThat(servicio.tasaExito(3, 0)).isNull();
            assertThat(servicio.tasaExito(-1, 10)).isNull();
            assertThat(servicio.tasaExito(null, 10)).isNull();
            assertThat(servicio.tasaExito(3, null)).isNull();
            assertThat(servicio.tasaExito((EvaluacionRegistrada) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Serie, último valor, acumulado y variación")
    class Metricas {

        @Test
        void serieIncluyeNullCuandoElObjetivoNoFueEvaluado() {
            List<SesionRegistrada> sesiones = List.of(
                    sesion(1, "2026-09-24", Map.of("obj-1", ev(2, 10))),
                    sesion(2, "2026-10-01", Map.of()),
                    sesion(3, "2026-10-08", Map.of("obj-1", ev(5, 10))));

            List<PuntoSerie> serie = servicio.serie(sesiones, "obj-1");

            assertThat(serie).extracting(PuntoSerie::session).containsExactly(1, 2, 3);
            assertThat(serie).extracting(PuntoSerie::value).containsExactly(20, null, 50);
            assertThat(serie.get(0).date()).isEqualTo(LocalDate.of(2026, 9, 24));
        }

        @Test
        void reproduceLosValoresDelDashboardDelPrototipo() {
            // obj-1: 20, 30, 50 → acumulado 33, último 50, variación +20
            assertThat(servicio.progresoAcumulado(HISTORIAL_TO, "obj-1")).isEqualTo(33);
            assertThat(servicio.ultimoPuntaje(HISTORIAL_TO, "obj-1")).isEqualTo(50);
            assertThat(servicio.ultimaVariacion(HISTORIAL_TO, "obj-1")).isEqualTo(20);
            assertThat(servicio.sesionesEvaluadas(HISTORIAL_TO, "obj-1")).isEqualTo(3);

            // obj-2: 20, 33, 50 → acumulado 34, último 50, variación +17
            assertThat(servicio.progresoAcumulado(HISTORIAL_TO, "obj-2")).isEqualTo(34);
            assertThat(servicio.ultimoPuntaje(HISTORIAL_TO, "obj-2")).isEqualTo(50);
            assertThat(servicio.ultimaVariacion(HISTORIAL_TO, "obj-2")).isEqualTo(17);
        }

        @Test
        void ignoraLasSesionesSinEvaluacionDelObjetivo() {
            List<SesionRegistrada> sesiones = List.of(
                    sesion(1, "2026-09-24", Map.of("obj-1", ev(2, 10))),
                    sesion(2, "2026-10-01", Map.of("obj-2", ev(1, 10))),
                    sesion(3, "2026-10-08", Map.of("obj-1", ev(5, 10))));

            assertThat(servicio.progresoAcumulado(sesiones, "obj-1")).isEqualTo(35);
            assertThat(servicio.ultimaVariacion(sesiones, "obj-1")).isEqualTo(30);
            assertThat(servicio.sesionesEvaluadas(sesiones, "obj-1")).isEqualTo(2);
        }

        @Test
        void sinSesionesDevuelveValoresNeutros() {
            List<SesionRegistrada> vacio = List.of();

            assertThat(servicio.progresoAcumulado(vacio, "obj-1")).isZero();
            assertThat(servicio.ultimoPuntaje(vacio, "obj-1")).isNull();
            assertThat(servicio.ultimaVariacion(vacio, "obj-1")).isNull();
            assertThat(servicio.sesionesEvaluadas(vacio, "obj-1")).isZero();
            assertThat(servicio.serie(vacio, "obj-1")).isEmpty();
        }

        @Test
        void laVariacionRequiereAlMenosDosSesionesEvaluadas() {
            List<SesionRegistrada> una = List.of(sesion(1, "2026-09-24", Map.of("obj-1", ev(2, 10))));
            assertThat(servicio.ultimaVariacion(una, "obj-1")).isNull();
        }
    }

    @Nested
    @DisplayName("Escala GAS (-2…+2) con línea base 10 y meta 80")
    class Gas {

        @ParameterizedTest(name = "puntaje {0} → nivel {1}")
        @CsvSource({
            "100, 2",
            "95, 2",
            "94, 1",
            "85, 1",
            "84, 0",
            "70, 0",
            "69, -1",
            "11, -1",
            "10, -2",
            "0, -2"
        })
        void clasificaSegunLasBandas(int puntaje, int nivelEsperado) {
            NivelGas nivel = servicio.calcularGas(puntaje, 10, 80);
            assertThat(nivel).isNotNull();
            assertThat(nivel.getNivel()).isEqualTo(nivelEsperado);
        }

        @Test
        void sinPuntajeNoHayNivel() {
            assertThat(servicio.calcularGas(null, 10, 80)).isNull();
        }

        @Test
        void losObjetivosDelPrototipoQuedanEnMenosUno() {
            // Último % = 50 para ambos objetivos (obj-1: base 10 / meta 80 · obj-2: base 20 / meta 70)
            assertThat(servicio.calcularGas(servicio.ultimoPuntaje(HISTORIAL_TO, "obj-1"), 10, 80))
                    .isEqualTo(NivelGas.MENOR_A_LO_ESPERADO);
            assertThat(servicio.calcularGas(servicio.ultimoPuntaje(HISTORIAL_TO, "obj-2"), 20, 70))
                    .isEqualTo(NivelGas.MENOR_A_LO_ESPERADO);
        }

        @Test
        void cadaNivelTieneEtiquetaDecisionYTono() {
            assertThat(NivelGas.porNivel(-2).getDecision()).isEqualTo("Bajar carga");
            assertThat(NivelGas.porNivel(-1).getDecision()).isEqualTo("Mantener con ajustes");
            assertThat(NivelGas.porNivel(0).getEtiqueta()).isEqualTo("Resultado esperado");
            assertThat(NivelGas.porNivel(1).getTono()).isEqualTo("success");
            assertThat(NivelGas.porNivel(2).getDecision()).isEqualTo("Subir carga / nuevo objetivo");
            assertThatThrownBy(() -> NivelGas.porNivel(3)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Cierre del ciclo")
    class Cierre {

        @Test
        void elGasDefinitivoSeHabilitaEnLaUltimaSesionOConElCicloCerrado() {
            assertThat(servicio.cicloEnCierre(EstadoCiclo.ABIERTO, 4, 12)).isFalse();
            assertThat(servicio.cicloEnCierre(EstadoCiclo.ABIERTO, 12, 12)).isTrue();
            assertThat(servicio.cicloEnCierre(EstadoCiclo.CERRADO, 4, 12)).isTrue();
        }
    }
}
