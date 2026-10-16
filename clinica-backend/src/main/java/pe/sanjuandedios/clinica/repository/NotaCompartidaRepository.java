package pe.sanjuandedios.clinica.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanjuandedios.clinica.domain.NotaCompartida;

public interface NotaCompartidaRepository extends JpaRepository<NotaCompartida, String> {

    @EntityGraph(attributePaths = {"especialidad", "profesional"})
    List<NotaCompartida> findByPacienteIdOrderByFechaDescIdDesc(String pacienteId);

    /** Correlativo para generar ids legibles de notas (ej. "note-101"). */
    @Query(value = "SELECT nextval('nota_compartida_seq')", nativeQuery = true)
    long siguienteSecuencia();
}
