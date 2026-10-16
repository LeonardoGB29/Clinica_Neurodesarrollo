package pe.sanjuandedios.clinica.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, String> {

    List<Especialidad> findAllByOrderByOrdenAsc();
}
