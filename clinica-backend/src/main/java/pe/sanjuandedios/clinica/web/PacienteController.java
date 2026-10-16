package pe.sanjuandedios.clinica.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.sanjuandedios.clinica.dto.CicloDTO;
import pe.sanjuandedios.clinica.dto.NotaCompartidaDTO;
import pe.sanjuandedios.clinica.dto.PacienteDTO;
import pe.sanjuandedios.clinica.dto.Requests.CrearCicloRequest;
import pe.sanjuandedios.clinica.dto.Requests.CrearNotaRequest;
import pe.sanjuandedios.clinica.dto.Vista360DTO;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.service.CicloService;
import pe.sanjuandedios.clinica.service.NotaService;
import pe.sanjuandedios.clinica.service.PacienteService;
import pe.sanjuandedios.clinica.service.Vista360Service;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Datos del paciente, ciclo activo por especialidad, vista 360° y notas compartidas")
public class PacienteController {

    private final PacienteService pacienteService;
    private final CicloService cicloService;
    private final Vista360Service vista360Service;
    private final NotaService notaService;

    @GetMapping("/{id}")
    @Operation(summary = "Datos básicos del paciente")
    public PacienteDTO obtener(@PathVariable String id) {
        return pacienteService.obtener(id);
    }

    @GetMapping("/{id}/ciclos")
    @Operation(summary = "Ciclo activo de la especialidad",
            description = "Ciclo abierto del paciente en la especialidad indicada: sesión actual, total y objetivos activos.")
    public CicloDTO cicloActivo(@PathVariable String id,
                                @Parameter(description = "Especialidad: TO, TL, TF o PS", example = "TO")
                                @RequestParam("especialidad") String especialidad) {
        return cicloService.obtenerActivo(id, especialidad);
    }

    @PostMapping("/{id}/ciclos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre un nuevo ciclo", description = "Crea un ciclo de 12 sesiones. 409 si ya existe un ciclo abierto de esa especialidad.")
    public CicloDTO crearCiclo(@PathVariable String id,
                               @Valid @RequestBody CrearCicloRequest request,
                               @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cicloService.crear(id, request, usuario);
    }

    @GetMapping("/{id}/vista-360")
    @Operation(summary = "Vista 360° interdisciplinaria",
            description = "Objetivos y progreso del paciente en las demás especialidades (se excluye la del profesional logueado).")
    public List<Vista360DTO> vista360(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return vista360Service.obtener(id, usuario);
    }

    @GetMapping("/{id}/notas")
    @Operation(summary = "Notas compartidas del equipo", description = "Misma forma que sharedNotes del prototipo.")
    public List<NotaCompartidaDTO> notas(@PathVariable String id) {
        return notaService.listar(id);
    }

    @PostMapping("/{id}/notas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publica una nota compartida")
    public NotaCompartidaDTO crearNota(@PathVariable String id,
                                       @Valid @RequestBody CrearNotaRequest request,
                                       @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return notaService.crear(id, request, usuario);
    }
}
