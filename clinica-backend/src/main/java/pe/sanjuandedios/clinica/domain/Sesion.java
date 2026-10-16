package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

/**
 * Sesión registrada de un ciclo. Es inmutable una vez guardada: Hibernate ignora
 * cualquier modificación ({@link Immutable}) y la base de datos rechaza UPDATE/DELETE
 * mediante un trigger (ver V1__esquema_inicial.sql).
 */
@Entity
@Table(name = "sesion")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private Ciclo ciclo;

    /** Número de la sesión dentro del ciclo (1..12). */
    @Column(nullable = false)
    private int numero;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(nullable = false, columnDefinition = "text")
    private String actividad;

    @Column(nullable = false, columnDefinition = "text")
    private String logro;

    @Column(columnDefinition = "text")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "registrado_en", nullable = false, updatable = false)
    private Instant registradoEn;

    @Builder.Default
    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL)
    private List<EvaluacionObjetivo> evaluaciones = new ArrayList<>();

    /** Agrega la evaluación de un objetivo (aciertos sobre intentos) a la sesión en construcción. */
    public EvaluacionObjetivo agregarEvaluacion(ObjetivoCiclo objetivo, int aciertos, int intentos) {
        EvaluacionObjetivo evaluacion = EvaluacionObjetivo.builder()
                .sesion(this)
                .objetivoCiclo(objetivo)
                .aciertos(aciertos)
                .intentos(intentos)
                .build();
        evaluaciones.add(evaluacion);
        return evaluacion;
    }
}
