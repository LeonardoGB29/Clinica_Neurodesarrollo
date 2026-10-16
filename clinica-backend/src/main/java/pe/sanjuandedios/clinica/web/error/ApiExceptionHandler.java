package pe.sanjuandedios.clinica.web.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Manejo global de errores en formato Problem Details (RFC 9457), con títulos y detalles en español.
 * Los errores estándar de Spring MVC (405, 404 de ruta, JSON mal formado, parámetros faltantes…)
 * los resuelve la clase base.
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail reglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return problema(HttpStatus.CONFLICT, "Conflicto con una regla de negocio", ex.getMessage(), request);
    }

    @ExceptionHandler(ValidacionException.class)
    ProblemDetail validacion(ValidacionException ex, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), request);
    }

    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    ProblemDetail accesoDenegado(RuntimeException ex, HttpServletRequest request) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail noAutenticado(AuthenticationException ex, HttpServletRequest request) {
        String detalle = switch (ex) {
            case BadCredentialsException ignored -> "Usuario o contraseña incorrectos.";
            case DisabledException ignored -> "El usuario está inactivo.";
            default -> "No fue posible autenticar la petición.";
        };
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", detalle, request);
    }

    /** Violaciones de restricciones de la base de datos (índices únicos, checks, trigger de inmutabilidad). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflicto de integridad",
                "La operación viola una restricción de la base de datos: " + ex.getMostSpecificCause().getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Si persiste, contacte al área de Sistemas.", request);
    }

    /** 400 con el detalle de cada campo inválido del cuerpo de la petición. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Hay campos inválidos en la solicitud.");
        problema.setTitle("Solicitud inválida");
        problema.setProperty("errores", errores);
        problema.setProperty("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(problema);
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle, HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }
}
