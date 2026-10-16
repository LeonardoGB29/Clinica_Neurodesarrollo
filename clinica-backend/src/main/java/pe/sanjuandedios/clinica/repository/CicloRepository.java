package pe.sanjuandedios.clinica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.Ciclo;
import pe.sanjuandedios.clinica.domain.EstadoCiclo;

public interface CicloRepository extends JpaRepository<Ciclo, String> {

    /** Ciclo con paciente, especialidad, profesional y objetivos (con su texto del banco) ya cargados. */
    @EntityGraph(attributePaths = {"paciente", "especialidad", "profesional", "objetivos", "objetivos.bancoObjetivo"})
    Optional<Ciclo> findDetalladoById(String id);

    @EntityGraph(attributePaths = {"paciente", "especialidad", "profesional", "objetivos", "objetivos.bancoObjetivo"})
    Optional<Ciclo> findByPacienteIdAndEspecialidadIdAndEstado(String pacienteId, String especialidadId, EstadoCiclo estado);

    @EntityGraph(attributePaths = {"especialidad", "profesional", "objetivos", "objetivos.bancoObjetivo"})
    List<Ciclo> findByPacienteIdAndEstado(String pacienteId, EstadoCiclo estado);

    long countByPacienteIdAndEspecialidadId(String pacienteId, String especialidadId);
}
