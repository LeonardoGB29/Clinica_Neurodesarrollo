package pe.sanjuandedios.clinica.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.sanjuandedios.clinica.dto.CicloDTO;
import pe.sanjuandedios.clinica.dto.ObjetivoActivoDTO;
import pe.sanjuandedios.clinica.dto.ProgresoCicloDTO;
import pe.sanjuandedios.clinica.dto.Requests.ActivarObjetivoRequest;
import pe.sanjuandedios.clinica.dto.Requests.AjustarObjetivoRequest;
import pe.sanjuandedios.clinica.dto.Requests.RegistrarSesionRequest;
import pe.sanjuandedios.clinica.dto.SesionDTO;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.service.CicloService;
import pe.sanjuandedios.clinica.service.ReportePdfService;

@RestController
@RequestMapping("/api/v1/ciclos")
@RequiredArgsConstructor
@Tag(name = "Ciclos", description = "Objetivos del ciclo (máx. 3), sesiones inmutables, progreso/GAS, cierre y reporte PDF")
public class CicloController {

    private final CicloService cicloService;
    private final ReportePdfService reportePdfService;

    @GetMapping("/{id}")
    @Operation(summary = "Ciclo por id (con objetivos activos)")
    public CicloDTO obtener(@PathVariable String id) {
        return cicloService.obtener(id);
    }

    // ------------------------------------------------------------------ objetivos

    @PostMapping("/{id}/objetivos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Activa un objetivo del banco en el ciclo", description = "409 si el ciclo ya tiene 3 objetivos activos, está cerrado o el objetivo ya está activo.")
    public ObjetivoActivoDTO activarObjetivo(@PathVariable String id,
                                             @Valid @RequestBody ActivarObjetivoRequest request,
                                             @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cicloService.activarObjetivo(id, request, usuario);
    }

    @PatchMapping("/{id}/objetivos/{objetivoId}")
    @Operation(summary = "Ajusta línea base / meta de un objetivo")
    public ObjetivoActivoDTO ajustarObjetivo(@PathVariable String id,
                                             @PathVariable String objetivoId,
                                             @Valid @RequestBody AjustarObjetivoRequest request,
                                             @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cicloService.ajustarObjetivo(id, objetivoId, request, usuario);
    }

    @DeleteMapping("/{id}/objetivos/{objetivoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retira un objetivo del ciclo", description = "El objetivo deja de estar activo; sus evaluaciones previas se conservan.")
    public void retirarObjetivo(@PathVariable String id,
                                @PathVariable String objetivoId,
                                @AuthenticationPrincipal UsuarioAutenticado usuario) {
        cicloService.retirarObjetivo(id, objetivoId, usuario);
    }

    // ------------------------------------------------------------------ sesiones

    @GetMapping("/{id}/sesiones")
    @Operation(summary = "Historial de sesiones (solo lectura)")
    public List<SesionDTO> sesiones(@PathVariable String id) {
        return cicloService.listarSesiones(id);
    }

    @GetMapping("/{id}/sesiones/{numero}")
    @Operation(summary = "Una sesión del historial (solo lectura)")
    public SesionDTO sesion(@PathVariable String id, @PathVariable int numero) {
        return cicloService.obtenerSesion(id, numero);
    }

    @PostMapping("/{id}/sesiones")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Guarda la sesión en curso",
            description = "Registra evaluaciones (aciertos/intentos por objetivo) y textos. La sesión queda inmutable; "
                    + "al guardar la sesión 12 el ciclo se cierra y se genera el GAS definitivo.")
    public SesionDTO registrarSesion(@PathVariable String id,
                                     @Valid @RequestBody RegistrarSesionRequest request,
                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cicloService.registrarSesion(id, request, usuario);
    }

    // ------------------------------------------------------------------ progreso y cierre

    @GetMapping("/{id}/progreso")
    @Operation(summary = "Progreso del ciclo", description = "% acumulado, última sesión, variación, serie por sesión y nivel GAS por objetivo.")
    public ProgresoCicloDTO progreso(@PathVariable String id) {
        return cicloService.progreso(id);
    }

    @PostMapping("/{id}/cierre")
    @Operation(summary = "Cierra el ciclo", description = "Cierre (anticipado o final) del ciclo; genera y persiste el resultado GAS de cada objetivo.")
    public ProgresoCicloDTO cerrar(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cicloService.cerrar(id, usuario);
    }

    // ------------------------------------------------------------------ reporte

    @GetMapping(value = "/{id}/reporte.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Reporte PDF de la ficha de evolución")
    public ResponseEntity<byte[]> reportePdf(@PathVariable String id) {
        byte[] pdf = reportePdfService.generar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ficha-evolucion-" + id + ".pdf\"")
                .body(pdf);
    }
}
