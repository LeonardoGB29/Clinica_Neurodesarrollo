package pe.sanjuandedios.clinica.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);
}
