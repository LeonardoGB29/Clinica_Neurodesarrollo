package pe.sanjuandedios.clinica.service;

import org.springframework.stereotype.Service;
import pe.sanjuandedios.clinica.domain.Rol;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.AccesoDenegadoException;

/**
 * Reglas de autorización por rol:
 * <ul>
 *   <li>TERAPEUTA / PSICOLOGO: leen todo y modifican solo los registros de su especialidad.</li>
 *   <li>COORDINADOR: lee y modifica cualquier especialidad.</li>
 *   <li>SISTEMAS: solo lectura de la información clínica.</li>
 * </ul>
 */
@Service
public class AutorizacionService {

    /** Verifica que el usuario pueda modificar registros clínicos de la especialidad indicada. */
    public void verificarPuedeEditarEspecialidad(String especialidadId, UsuarioAutenticado usuario) {
        if (usuario == null) {
            throw new AccesoDenegadoException("No hay un usuario autenticado.");
        }
        if (usuario.getRol() == Rol.COORDINADOR) {
            return;
        }
        if (usuario.getRol() == Rol.SISTEMAS) {
            throw new AccesoDenegadoException("El rol SISTEMAS solo tiene acceso de lectura a la información clínica.");
        }
        if (usuario.getEspecialidadId() == null || !usuario.getEspecialidadId().equalsIgnoreCase(especialidadId)) {
            throw new AccesoDenegadoException(
                    "Solo los profesionales de la especialidad %s o la coordinación pueden modificar este registro."
                            .formatted(especialidadId));
        }
    }
}
