package pe.sanjuandedios.clinica.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sanjuandedios.clinica.dto.ProfesionalDTO;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.service.ProfesionalService;

@RestController
@RequestMapping("/api/v1/profesionales")
@RequiredArgsConstructor
@Tag(name = "Profesionales")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    @GetMapping("/me")
    @Operation(summary = "Profesional logueado", description = "Nombre, especialidad y colegiatura del profesional autenticado (cabecera de la ficha).")
    public ProfesionalDTO me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return profesionalService.me(usuario);
    }
}
