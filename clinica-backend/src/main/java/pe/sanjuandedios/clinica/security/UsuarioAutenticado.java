package pe.sanjuandedios.clinica.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.sanjuandedios.clinica.domain.Rol;

/** Principal autenticado: usuario, rol y, si corresponde, el profesional y especialidad asociados. */
@Getter
public class UsuarioAutenticado implements UserDetails {

    private final String id;
    private final String nombreUsuario;
    private final String claveHash;
    private final Rol rol;
    private final boolean activo;
    /** Id del profesional vinculado al usuario; {@code null} para cuentas sin profesional (coordinación, sistemas). */
    private final String profesionalId;
    /** Especialidad del profesional vinculado; {@code null} si no hay profesional. */
    private final String especialidadId;

    public UsuarioAutenticado(String id, String nombreUsuario, String claveHash, Rol rol, boolean activo,
                              String profesionalId, String especialidadId) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.claveHash = claveHash;
        this.rol = rol;
        this.activo = activo;
        this.profesionalId = profesionalId;
        this.especialidadId = especialidadId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return claveHash;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    public boolean esCoordinador() {
        return rol == Rol.COORDINADOR;
    }

    public boolean esSistemas() {
        return rol == Rol.SISTEMAS;
    }

    public boolean tieneProfesional() {
        return profesionalId != null;
    }
}
