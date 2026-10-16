package pe.sanjuandedios.clinica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.Sesion;

public interface SesionRepository extends JpaRepository<Sesion, Long> {

    /** Historial del ciclo con evaluaciones, objetivos y profesional ya cargados (evita N+1). */
    @EntityGraph(attributePaths = {"profesional", "evaluaciones", "evaluaciones.objetivoCiclo"})
    List<Sesion> findByCicloIdOrderByNumeroAsc(String cicloId);

    @EntityGraph(attributePaths = {"profesional", "evaluaciones", "evaluaciones.objetivoCiclo"})
    Optional<Sesion> findByCicloIdAndNumero(String cicloId, int numero);

    boolean existsByCicloIdAndNumero(String cicloId, int numero);
}
