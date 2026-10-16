package pe.sanjuandedios.clinica.dto;

/** Respuesta de POST /auth/login. */
public record LoginResponse(String token, String tipo, long expiraEnSegundos, UsuarioSesionDTO usuario) {

    /** Datos mínimos del usuario autenticado. */
    public record UsuarioSesionDTO(String id, String nombreUsuario, String rol, String profesionalId, String specialtyId) {}
}
