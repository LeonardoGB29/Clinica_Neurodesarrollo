package pe.sanjuandedios.clinica.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sanjuandedios.clinica.dto.EspecialidadDTO;
import pe.sanjuandedios.clinica.service.EspecialidadService;

@RestController
@RequestMapping("/api/v1/especialidades")
@RequiredArgsConstructor
@Tag(name = "Especialidades", description = "Catálogo de especialidades del centro (TO, TL, TF, PS)")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @GetMapping
    @Operation(summary = "Lista las especialidades", description = "Misma forma que SPECIALTIES del prototipo (id, name, short, college, color).")
    public List<EspecialidadDTO> listar() {
        return especialidadService.listar();
    }
}
