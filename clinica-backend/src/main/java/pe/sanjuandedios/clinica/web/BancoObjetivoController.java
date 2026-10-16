package pe.sanjuandedios.clinica.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.sanjuandedios.clinica.dto.BancoObjetivoDTO;
import pe.sanjuandedios.clinica.dto.Requests.CrearBancoObjetivoRequest;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.service.BancoObjetivoService;

@RestController
@RequestMapping("/api/v1/banco-objetivos")
@RequiredArgsConstructor
@Tag(name = "Banco de objetivos", description = "Objetivos reutilizables por especialidad")
public class BancoObjetivoController {

    private final BancoObjetivoService bancoObjetivoService;

    @GetMapping
    @Operation(summary = "Banco de objetivos de la especialidad")
    public List<BancoObjetivoDTO> listar(@Parameter(description = "Especialidad: TO, TL, TF o PS", example = "TO")
                                         @RequestParam("especialidad") String especialidad) {
        return bancoObjetivoService.listar(especialidad);
    }

    @PostMapping
    @Operation(summary = "Crea un objetivo en el banco",
            description = "201 si se creó; 200 con el objetivo existente si ya había uno con el mismo texto en la especialidad.")
    public ResponseEntity<BancoObjetivoDTO> crear(@Valid @RequestBody CrearBancoObjetivoRequest request,
                                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        BancoObjetivoService.Resultado resultado = bancoObjetivoService.crear(request, usuario);
        return ResponseEntity.status(resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK).body(resultado.objetivo());
    }
}
