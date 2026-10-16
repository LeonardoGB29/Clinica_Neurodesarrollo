package pe.sanjuandedios.clinica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.ResultadoGas;

public interface ResultadoGasRepository extends JpaRepository<ResultadoGas, Long> {

    Optional<ResultadoGas> findByObjetivoCicloId(String objetivoCicloId);

    /** Resultados GAS de todos los objetivos de un ciclo. */
    @EntityGraph(attributePaths = "objetivoCiclo")
    List<ResultadoGas> findByObjetivoCicloCicloId(String cicloId);
}
