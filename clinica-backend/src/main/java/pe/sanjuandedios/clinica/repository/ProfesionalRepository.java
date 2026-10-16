package pe.sanjuandedios.clinica.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.Profesional;

public interface ProfesionalRepository extends JpaRepository<Profesional, String> {

    @EntityGraph(attributePaths = "especialidad")
    Optional<Profesional> findByUsuarioId(String usuarioId);
}
