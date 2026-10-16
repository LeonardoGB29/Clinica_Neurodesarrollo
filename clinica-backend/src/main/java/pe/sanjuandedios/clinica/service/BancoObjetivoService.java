package pe.sanjuandedios.clinica.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.domain.BancoObjetivo;
import pe.sanjuandedios.clinica.domain.Especialidad;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.dto.BancoObjetivoDTO;
import pe.sanjuandedios.clinica.dto.Requests.CrearBancoObjetivoRequest;
import pe.sanjuandedios.clinica.mapper.ObjetivoMapper;
import pe.sanjuandedios.clinica.repository.BancoObjetivoRepository;
import pe.sanjuandedios.clinica.repository.EspecialidadRepository;
import pe.sanjuandedios.clinica.repository.ProfesionalRepository;
import pe.sanjuandedios.clinica.security.UsuarioAutenticado;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;
import pe.sanjuandedios.clinica.web.error.ValidacionException;

/** Banco de objetivos por especialidad, reutilizable en futuros ciclos. */
@Service
@RequiredArgsConstructor
public class BancoObjetivoService {

    private final BancoObjetivoRepository bancoObjetivoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AutorizacionService autorizacionService;
    private final ObjetivoMapper objetivoMapper;

    /** Resultado de la creación: el objetivo y si fue creado ({@code true}) o ya existía ({@code false}). */
    public record Resultado(BancoObjetivoDTO objetivo, boolean creado) {}

    @Transactional(readOnly = true)
    public List<BancoObjetivoDTO> listar(String especialidadId) {
        String esp = normalizarEspecialidad(especialidadId);
        if (!especialidadRepository.existsById(esp)) {
            throw new RecursoNoEncontradoException("Especialidad", esp);
        }
        return objetivoMapper.toBancoDtoList(bancoObjetivoRepository.findByEspecialidadIdAndActivoTrueOrderByIdAsc(esp));
    }

    /**
     * Crea un objetivo en el banco de la especialidad. Si ya existe uno con el mismo texto
     * (sin distinguir mayúsculas) se devuelve el existente en lugar de duplicarlo.
     */
    @Transactional
    public Resultado crear(CrearBancoObjetivoRequest request, UsuarioAutenticado usuario) {
        String esp = request.specialtyId() != null && !request.specialtyId().isBlank()
                ? normalizarEspecialidad(request.specialtyId())
                : usuario.getEspecialidadId();
        if (esp == null) {
            throw new ValidacionException("Indica la especialidad del objetivo (specialtyId).");
        }
        autorizacionService.verificarPuedeEditarEspecialidad(esp, usuario);
        Especialidad especialidad = especialidadRepository.findById(esp)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", esp));

        String texto = request.text().trim().replaceAll("\\s+", " ");
        Optional<BancoObjetivo> existente = bancoObjetivoRepository.findByEspecialidadIdAndTextoIgnoreCase(esp, texto);
        if (existente.isPresent()) {
            BancoObjetivo b = existente.get();
            b.setActivo(true);
            return new Resultado(objetivoMapper.toDto(b), false);
        }

        Profesional autor = usuario.tieneProfesional()
                ? profesionalRepository.findById(usuario.getProfesionalId()).orElse(null)
                : null;
        BancoObjetivo nuevo = BancoObjetivo.builder()
                .id("%s-%03d".formatted(esp.toLowerCase(), bancoObjetivoRepository.siguienteSecuencia()))
                .especialidad(especialidad)
                .texto(texto)
                .creadoPor(autor)
                .activo(true)
                .build();
        return new Resultado(objetivoMapper.toDto(bancoObjetivoRepository.save(nuevo)), true);
    }

    static String normalizarEspecialidad(String especialidadId) {
        if (especialidadId == null || especialidadId.isBlank()) {
            throw new ValidacionException("El parámetro 'especialidad' es obligatorio (TO, TL, TF o PS).");
        }
        return especialidadId.trim().toUpperCase();
    }
}
