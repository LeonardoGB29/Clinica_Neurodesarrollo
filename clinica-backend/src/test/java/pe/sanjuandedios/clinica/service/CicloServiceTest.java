package pe.sanjuandedios.clinica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.sanjuandedios.clinica.domain.BancoObjetivo;
import pe.sanjuandedios.clinica.domain.Ciclo;
import pe.sanjuandedios.clinica.domain.Especialidad;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;
import pe.sanjuandedios.clinica.domain.ObjetivoCiclo;
import pe.sanjuandedios.clinica.domain.Paciente;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.domain.ResultadoGas;
import pe.sanjuandedios.clinica.domain.Rol;
import pe.sanjuandedios.clinica.domain.Sesion;
import pe.sanjuandedios.clinica.domain.progreso.ProgresoService;
import pe.sanjuandedios.clinica.dto.Requests.ActivarObjetivoRequest;
import pe.sanjuandedios.clinica.dto.Requests.EvaluacionRequest;
import pe.sanjuandedios.clinica.dto.Requests.RegistrarSesionRequest;
import pe.sanjuandedios.clinica.mapper.CicloMapper;
import pe.sanjuandedios.clinica.mapper.ObjetivoMapper;
import pe.sanjuandedios.clinica.mapper.SesionMapper;
import pe.sanjuandedios.clinica.repository.BancoObjetivoRepository;
import pe.sanjuandedios.clinica.repository.CicloRepository;
import pe.sanjuandedios.clinica.repository.EspecialidadRepository;
import pe.sanjuandedios.clinica.repository.ObjetivoCicloRepository;
import pe.sanjuandedios.clinica.repository.PacienteRepository;
import pe.sanjuandedios.clinica.repository.ProfesionalRepository;
import pe.sanjuandedios.clinica.repository.ResultadoGasRepository;
import pe.sanjuandedios.clinica.repository.SesionRepository;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.AccesoDenegadoException;
import pe.sanjuandedios.clinica.web.error.ReglaNegocioException;
import pe.sanjuandedios.clinica.web.error.ValidacionException;

/** Reglas de negocio del ciclo sin base de datos (repositorios simulados). */
@ExtendWith(MockitoExtension.class)
class CicloServiceTest {

    private static final String CICLO_ID = "cic-2026-TO-0231-03";
    private static final UsuarioAutenticado TERAPEUTA_TO =
            new UsuarioAutenticado("usr-001", "mquispe", "hash", Rol.TERAPEUTA, true, "prof-017", "TO");
    private static final UsuarioAutenticado TERAPEUTA_TL =
            new UsuarioAutenticado("usr-002", "cmendoza", "hash", Rol.TERAPEUTA, true, "prof-021", "TL");

    @Mock CicloRepository cicloRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock EspecialidadRepository especialidadRepository;
    @Mock ProfesionalRepository profesionalRepository;
    @Mock BancoObjetivoRepository bancoObjetivoRepository;
    @Mock ObjetivoCicloRepository objetivoCicloRepository;
    @Mock SesionRepository sesionRepository;
    @Mock ResultadoGasRepository resultadoGasRepository;
    @Spy ProgresoService progresoService = new ProgresoService();
    @Spy AutorizacionService autorizacionService = new AutorizacionService();
    @Mock CicloMapper cicloMapper;
    @Mock ObjetivoMapper objetivoMapper;
    @Mock SesionMapper sesionMapper;

    @InjectMocks CicloService servicio;

    private Especialidad to;
    private Profesional profesional;
    private Ciclo ciclo;

    @BeforeEach
    void setUp() {
        to = Especialidad.builder().id("TO").nombre("Terapia Ocupacional").nombreCorto("T. Ocupacional")
                .colegioProfesional("CTMP").color("#0083A8").orden(1).build();
        profesional = Profesional.builder().id("prof-017").nombres("Lic. María Fernanda Quispe Rojas").iniciales("MQ")
                .especialidad(to).colegio("CTMP").nroColegiatura("14582").build();
        Paciente paciente = Paciente.builder().id("pac-0231").nroHistoria("HC-2024-0231")
                .nombres("Mateo Alejandro Huamán Torres").iniciales("MH").fechaNac(LocalDate.of(2019, 3, 12)).sexo("M").build();
        ciclo = Ciclo.builder().id(CICLO_ID).paciente(paciente).especialidad(to).profesional(profesional)
                .fechaInicio(LocalDate.of(2026, 9, 24)).sesionActual(4).build();
    }

    private ObjetivoCiclo objetivo(String id, int orden, int lineaBase, int meta) {
        BancoObjetivo banco = BancoObjetivo.builder().id("to-00" + orden).especialidad(to).texto("Objetivo " + orden).build();
        return ObjetivoCiclo.builder().id(id).ciclo(ciclo).bancoObjetivo(banco)
                .lineaBase(lineaBase).meta(meta).sesionInicio(1).orden(orden).build();
    }

    private BancoObjetivo bancoTo(String id) {
        return BancoObjetivo.builder().id(id).especialidad(to).texto("Tolerar texturas diversas").build();
    }

    @Test
    void rechazaElCuartoObjetivoActivoConConflicto() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        when(bancoObjetivoRepository.findById("to-004")).thenReturn(Optional.of(bancoTo("to-004")));
        when(objetivoCicloRepository.existsByCicloIdAndBancoObjetivoIdAndActivoTrue(CICLO_ID, "to-004")).thenReturn(false);
        when(objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(CICLO_ID)).thenReturn(List.of(
                objetivo("obj-1", 1, 10, 80), objetivo("obj-2", 2, 20, 70), objetivo("obj-3", 3, 0, 80)));

        assertThatThrownBy(() -> servicio.activarObjetivo(CICLO_ID, new ActivarObjetivoRequest("to-004", 0, 80), TERAPEUTA_TO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("máximo 3");
        verify(objetivoCicloRepository, never()).save(any());
    }

    @Test
    void activaElObjetivoEnElPrimerOrdenLibreYEnLaSesionActual() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        when(bancoObjetivoRepository.findById("to-004")).thenReturn(Optional.of(bancoTo("to-004")));
        when(objetivoCicloRepository.existsByCicloIdAndBancoObjetivoIdAndActivoTrue(CICLO_ID, "to-004")).thenReturn(false);
        // El orden 2 está libre porque ese objetivo fue retirado
        when(objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(CICLO_ID))
                .thenReturn(List.of(objetivo("obj-1", 1, 10, 80), objetivo("obj-3", 3, 0, 80)));
        when(objetivoCicloRepository.siguienteSecuencia()).thenReturn(101L);
        when(objetivoCicloRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        servicio.activarObjetivo(CICLO_ID, new ActivarObjetivoRequest("to-004", null, null), TERAPEUTA_TO);

        ArgumentCaptor<ObjetivoCiclo> captor = ArgumentCaptor.forClass(ObjetivoCiclo.class);
        verify(objetivoCicloRepository).save(captor.capture());
        ObjetivoCiclo creado = captor.getValue();
        assertThat(creado.getId()).isEqualTo("obj-101");
        assertThat(creado.getOrden()).isEqualTo(2);
        assertThat(creado.getSesionInicio()).isEqualTo(4);
        assertThat(creado.getLineaBase()).isZero();
        assertThat(creado.getMeta()).isEqualTo(80);
        assertThat(creado.isActivo()).isTrue();
    }

    @Test
    void rechazaCambiosEnUnCicloCerrado() {
        ciclo.setEstado(EstadoCiclo.CERRADO);
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));

        assertThatThrownBy(() -> servicio.activarObjetivo(CICLO_ID, new ActivarObjetivoRequest("to-004", 0, 80), TERAPEUTA_TO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cerrado");
    }

    @Test
    void soloLaEspecialidadDelCicloOLaCoordinacionPuedenEditarlo() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));

        assertThatThrownBy(() -> servicio.retirarObjetivo(CICLO_ID, "obj-1", TERAPEUTA_TL))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void rechazaAciertosMayoresQueLosIntentos() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        when(objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(CICLO_ID))
                .thenReturn(List.of(objetivo("obj-1", 1, 10, 80)));
        RegistrarSesionRequest request = new RegistrarSesionRequest(4, LocalDate.of(2026, 10, 15),
                Map.of("obj-1", new EvaluacionRequest(11, 10)), "Actividad", "Logro", null);

        assertThatThrownBy(() -> servicio.registrarSesion(CICLO_ID, request, TERAPEUTA_TO))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("aciertos");
        verify(sesionRepository, never()).save(any());
    }

    @Test
    void rechazaEvaluacionesDeObjetivosQueNoEstanActivos() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        when(objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(CICLO_ID))
                .thenReturn(List.of(objetivo("obj-1", 1, 10, 80)));
        RegistrarSesionRequest request = new RegistrarSesionRequest(null, null,
                Map.of("obj-9", new EvaluacionRequest(5, 10)), "Actividad", "Logro", null);

        assertThatThrownBy(() -> servicio.registrarSesion(CICLO_ID, request, TERAPEUTA_TO))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("obj-9");
    }

    @Test
    void rechazaRegistrarUnaSesionDistintaDeLaEnCurso() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        RegistrarSesionRequest request = new RegistrarSesionRequest(7, null,
                Map.of("obj-1", new EvaluacionRequest(5, 10)), "Actividad", "Logro", null);

        assertThatThrownBy(() -> servicio.registrarSesion(CICLO_ID, request, TERAPEUTA_TO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sesión en curso");
    }

    @Test
    void guardarLaSesionAvanzaElCiclo() {
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        when(objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(CICLO_ID))
                .thenReturn(List.of(objetivo("obj-1", 1, 10, 80)));
        when(profesionalRepository.findById("prof-017")).thenReturn(Optional.of(profesional));
        when(sesionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RegistrarSesionRequest request = new RegistrarSesionRequest(4, LocalDate.of(2026, 10, 15),
                Map.of("obj-1", new EvaluacionRequest(6, 10)), "  Actividad  ", "Logro", "   ");

        servicio.registrarSesion(CICLO_ID, request, TERAPEUTA_TO);

        ArgumentCaptor<Sesion> captor = ArgumentCaptor.forClass(Sesion.class);
        verify(sesionRepository).save(captor.capture());
        Sesion guardada = captor.getValue();
        assertThat(guardada.getNumero()).isEqualTo(4);
        assertThat(guardada.getActividad()).isEqualTo("Actividad");
        assertThat(guardada.getObservaciones()).isNull();
        assertThat(guardada.getEvaluaciones()).hasSize(1);
        assertThat(guardada.getEvaluaciones().get(0).getAciertos()).isEqualTo(6);
        assertThat(ciclo.getSesionActual()).isEqualTo(5);
        assertThat(ciclo.getEstado()).isEqualTo(EstadoCiclo.ABIERTO);
        verify(resultadoGasRepository, never()).save(any());
    }

    @Test
    void laSesion12CierraElCicloYGeneraElGasDefinitivo() {
        ciclo.setSesionActual(12);
        ObjetivoCiclo obj1 = objetivo("obj-1", 1, 10, 80);
        AtomicReference<Sesion> guardada = new AtomicReference<>();
        when(cicloRepository.findDetalladoById(CICLO_ID)).thenReturn(Optional.of(ciclo));
        when(objetivoCicloRepository.findByCicloIdAndActivoTrueOrderByOrdenAsc(CICLO_ID)).thenReturn(List.of(obj1));
        when(profesionalRepository.findById("prof-017")).thenReturn(Optional.of(profesional));
        when(sesionRepository.save(any())).thenAnswer(inv -> {
            guardada.set(inv.getArgument(0));
            return guardada.get();
        });
        when(sesionRepository.findByCicloIdOrderByNumeroAsc(CICLO_ID)).thenAnswer(inv -> List.of(guardada.get()));
        when(resultadoGasRepository.findByObjetivoCicloId("obj-1")).thenReturn(Optional.empty());
        RegistrarSesionRequest request = new RegistrarSesionRequest(12, LocalDate.of(2026, 12, 10),
                Map.of("obj-1", new EvaluacionRequest(9, 10)), "Actividad final", "Logro final", "Cierre del ciclo");

        servicio.registrarSesion(CICLO_ID, request, TERAPEUTA_TO);

        assertThat(ciclo.getEstado()).isEqualTo(EstadoCiclo.CERRADO);
        assertThat(ciclo.getFechaCierre()).isEqualTo(LocalDate.of(2026, 12, 10));
        assertThat(ciclo.getSesionActual()).isEqualTo(13);
        ArgumentCaptor<ResultadoGas> captor = ArgumentCaptor.forClass(ResultadoGas.class);
        verify(resultadoGasRepository).save(captor.capture());
        // 90 % con meta 80 → 90 ≥ 85 → nivel +1 "Subir carga"
        assertThat(captor.getValue().getPuntaje()).isEqualTo(90);
        assertThat(captor.getValue().getNivel()).isEqualTo(1);
        assertThat(captor.getValue().getDecision()).isEqualTo("Subir carga");
    }
}
