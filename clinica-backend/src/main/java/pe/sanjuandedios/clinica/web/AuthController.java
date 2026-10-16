package pe.sanjuandedios.clinica.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sanjuandedios.clinica.dto.LoginResponse;
import pe.sanjuandedios.clinica.dto.LoginResponse.UsuarioSesionDTO;
import pe.sanjuandedios.clinica.dto.Requests.LoginRequest;
import pe.sanjuandedios.clinica.security.JwtService;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión y emisión del JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Inicia sesión", description = "Valida usuario y contraseña y devuelve un JWT (Bearer).")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.clave()));
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        return new LoginResponse(
                jwtService.generar(usuario),
                "Bearer",
                jwtService.segundosDeVigencia(),
                new UsuarioSesionDTO(usuario.getId(), usuario.getUsername(), usuario.getRol().name(),
                        usuario.getProfesionalId(), usuario.getEspecialidadId()));
    }
}
