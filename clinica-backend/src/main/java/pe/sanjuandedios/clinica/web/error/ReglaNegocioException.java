package pe.sanjuandedios.clinica.web.error;

/** 409 – la operación viola una regla de negocio (ej. más de 3 objetivos activos, ciclo cerrado). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
