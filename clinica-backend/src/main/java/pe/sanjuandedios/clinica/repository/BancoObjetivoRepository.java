package pe.sanjuandedios.clinica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanjuandedios.clinica.domain.BancoObjetivo;

public interface BancoObjetivoRepository extends JpaRepository<BancoObjetivo, String> {

    @EntityGraph(attributePaths = "especialidad")
    List<BancoObjetivo> findByEspecialidadIdAndActivoTrueOrderByIdAsc(String especialidadId);

    @EntityGraph(attributePaths = "especialidad")
    Optional<BancoObjetivo> findByEspecialidadIdAndTextoIgnoreCase(String especialidadId, String texto);

    /** Correlativo para generar ids legibles del banco (ej. "to-101"). */
    @Query(value = "SELECT nextval('banco_objetivo_seq')", nativeQuery = true)
    long siguienteSecuencia();
}
