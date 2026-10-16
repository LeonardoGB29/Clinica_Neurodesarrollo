package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Ciclo terapéutico de un paciente en una especialidad: 12 sesiones (12 semanas),
 * máximo 3 objetivos activos. Solo puede haber un ciclo abierto por paciente y especialidad.
 */
@Entity
@Table(name = "ciclo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ciclo {

    public static final int TOTAL_SESIONES_POR_DEFECTO = 12;
    public static final int MAX_OBJETIVOS_POR_DEFECTO = 3;

    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    /** Profesional a cargo del ciclo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Builder.Default
    @Column(name = "total_sesiones", nullable = false)
    private int totalSesiones = TOTAL_SESIONES_POR_DEFECTO;

    /** Número de la sesión en curso (= sesiones registradas + 1). */
    @Builder.Default
    @Column(name = "sesion_actual", nullable = false)
    private int sesionActual = 1;

    @Column(length = 60)
    private String frecuencia;

    @Builder.Default
    @Column(name = "max_objetivos", nullable = false)
    private int maxObjetivos = MAX_OBJETIVOS_POR_DEFECTO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoCiclo estado = EstadoCiclo.ABIERTO;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    /** Todos los objetivos del ciclo (activos y retirados). */
    @Builder.Default
    @OneToMany(mappedBy = "ciclo")
    @OrderBy("orden ASC")
    private List<ObjetivoCiclo> objetivos = new ArrayList<>();

    public boolean estaAbierto() {
        return estado == EstadoCiclo.ABIERTO;
    }

    /** Objetivos vigentes del ciclo ordenados por su posición (máx. 3). */
    public List<ObjetivoCiclo> getObjetivosActivos() {
        return objetivos.stream()
                .filter(ObjetivoCiclo::isActivo)
                .sorted(Comparator.comparingInt(ObjetivoCiclo::getOrden))
                .toList();
    }
}
