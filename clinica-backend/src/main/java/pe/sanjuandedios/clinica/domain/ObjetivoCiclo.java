package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Objetivo activado en un ciclo (máx. 3 activos por ciclo).
 * lineaBase: % de éxito en la evaluación inicial; meta: % esperado al cierre (nivel 0 de la escala GAS).
 */
@Entity
@Table(name = "objetivo_ciclo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjetivoCiclo {

    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private Ciclo ciclo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_objetivo_id", nullable = false)
    private BancoObjetivo bancoObjetivo;

    @Column(name = "linea_base", nullable = false)
    private int lineaBase;

    @Column(nullable = false)
    private int meta;

    /** Sesión del ciclo en la que se activó el objetivo. */
    @Column(name = "sesion_inicio", nullable = false)
    private int sesionInicio;

    /** Posición 1..3 entre los objetivos activos del ciclo. */
    @Column(nullable = false)
    private int orden;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "retirado_en")
    private Instant retiradoEn;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    /** Retira el objetivo del ciclo conservando el historial de evaluaciones. */
    public void retirar() {
        this.activo = false;
        this.retiradoEn = Instant.now();
    }
}
