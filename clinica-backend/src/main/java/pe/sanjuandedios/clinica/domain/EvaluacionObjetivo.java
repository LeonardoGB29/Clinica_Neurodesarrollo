package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Evaluación de un objetivo en una sesión: % de éxito = aciertos / intentos (ej. 3 de 10 = 30 %). */
@Entity
@Table(name = "evaluacion_objetivo")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionObjetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private Sesion sesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "objetivo_ciclo_id", nullable = false)
    private ObjetivoCiclo objetivoCiclo;

    @Column(nullable = false)
    private int aciertos;

    @Column(nullable = false)
    private int intentos;
}
