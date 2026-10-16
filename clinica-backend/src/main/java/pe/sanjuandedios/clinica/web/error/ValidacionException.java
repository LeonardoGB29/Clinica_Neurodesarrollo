package pe.sanjuandedios.clinica.web.error;

/** 400 – los datos enviados no son válidos (ej. aciertos mayores que intentos). */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
