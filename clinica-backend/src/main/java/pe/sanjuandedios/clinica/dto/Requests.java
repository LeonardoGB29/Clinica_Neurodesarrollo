package pe.sanjuandedios.clinica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Map;

/** Cuerpos de las peticiones de escritura. Los nombres de campo coinciden con los del front. */
public final class Requests {

    private Requests() {}

    /** POST /auth/login */
    public record LoginRequest(@NotBlank String usuario, @NotBlank String clave) {}

    /** POST /banco-objetivos. Si no se indica {@code specialtyId} se usa la especialidad del profesional logueado. */
    public record CrearBancoObjetivoRequest(
            @Size(max = 4) String specialtyId,
            @NotBlank @Size(min = 8, max = 300, message = "Describe el objetivo con más detalle (mín. 8 caracteres).") String text) {}

    /** POST /ciclos/{id}/objetivos. Por defecto baseline = 0 y target = 80 (como en el prototipo). */
    public record ActivarObjetivoRequest(
            @NotBlank String bankId,
            @Min(0) @Max(100) Integer baseline,
            @Min(0) @Max(100) Integer target) {}

    /** PATCH /ciclos/{id}/objetivos/{objetivoId}: ajusta línea base y/o meta. */
    public record AjustarObjetivoRequest(
            @Min(0) @Max(100) Integer baseline,
            @Min(0) @Max(100) Integer target) {}

    /** Aciertos / intentos de un objetivo en la sesión. */
    public record EvaluacionRequest(
            @NotNull @Min(0) Integer hits,
            @NotNull @Min(1) Integer total) {}

    /**
     * POST /ciclos/{id}/sesiones. {@code number} es opcional: si se envía debe coincidir con la sesión en curso.
     * {@code evaluations} está indexado por id de objetivo activo del ciclo.
     */
    public record RegistrarSesionRequest(
            Integer number,
            LocalDate date,
            @NotEmpty Map<String, @Valid EvaluacionRequest> evaluations,
            @NotBlank(message = "Describe la actividad realizada.") String activity,
            @NotBlank(message = "Registra el logro de la sesión.") String achievement,
            String observations) {}

    /** POST /pacientes/{id}/ciclos: abre un nuevo ciclo de 12 sesiones. */
    public record CrearCicloRequest(
            @NotBlank @Size(max = 4) String specialtyId,
            String professionalId,
            LocalDate startDate,
            @Size(max = 60) String frequency,
            @Min(1) @Max(52) Integer totalSessions) {}

    /** POST /pacientes/{id}/notas. */
    public record CrearNotaRequest(
            @NotBlank @Size(max = 2000) String text,
            LocalDate date) {}
}
