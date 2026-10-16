package pe.sanjuandedios.clinica.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.domain.Usuario;
import pe.sanjuandedios.clinica.repository.ProfesionalRepository;
import pe.sanjuandedios.clinica.repository.UsuarioRepository;

/** Carga el usuario desde la tabla {@code usuario} y lo enlaza con su profesional (si existe). */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuarioIgnoreCase(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario));
        Profesional profesional = profesionalRepository.findByUsuarioId(usuario.getId()).orElse(null);
        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getClaveHash(),
                usuario.getRol(),
                usuario.isActivo(),
                profesional == null ? null : profesional.getId(),
                profesional == null ? null : profesional.getEspecialidad().getId());
    }
}
