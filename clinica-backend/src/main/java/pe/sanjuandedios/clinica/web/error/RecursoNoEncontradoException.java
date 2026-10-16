package pe.sanjuandedios.clinica.web.error;

/** 404 – el recurso solicitado no existe. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, String id) {
        super("No se encontró %s con id '%s'.".formatted(recurso, id));
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
