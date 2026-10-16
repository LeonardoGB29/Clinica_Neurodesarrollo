package pe.sanjuandedios.clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanjuandedios.clinica.domain.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, String> {}
