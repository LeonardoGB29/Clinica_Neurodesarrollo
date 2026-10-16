package pe.sanjuandedios.clinica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanjuandedios.clinica.domain.ObjetivoCiclo;

public interface ObjetivoCicloRepository extends JpaRepository<ObjetivoCiclo, String> {

    @EntityGraph(attributePaths = "bancoObjetivo")
    List<ObjetivoCiclo> findByCicloIdAndActivoTrueOrderByOrdenAsc(String cicloId);

    long countByCicloIdAndActivoTrue(String cicloId);

    @EntityGraph(attributePaths = "bancoObjetivo")
    Optional<ObjetivoCiclo> findByIdAndCicloId(String id, String cicloId);

    boolean existsByCicloIdAndBancoObjetivoIdAndActivoTrue(String cicloId, String bancoObjetivoId);

    /** Correlativo para generar ids legibles de objetivos del ciclo (ej. "obj-101"). */
    @Query(value = "SELECT nextval('objetivo_ciclo_seq')", nativeQuery = true)
    long siguienteSecuencia();
}
