package pe.sanjuandedios.clinica.web.error;

/** 403 – el usuario autenticado no puede realizar la operación (otra especialidad, rol de solo lectura). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
