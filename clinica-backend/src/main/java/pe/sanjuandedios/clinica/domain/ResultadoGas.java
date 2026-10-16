package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Resultado definitivo de la escala GAS (-2…+2) de un objetivo, generado al cerrar el ciclo. */
@Entity
@Table(name = "resultado_gas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoGas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "objetivo_ciclo_id", nullable = false, unique = true)
    private ObjetivoCiclo objetivoCiclo;

    /** Nivel GAS: -2, -1, 0, +1, +2. */
    @Column(nullable = false)
    private int nivel;

    /** % de éxito con el que se calculó el nivel (última sesión registrada). */
    @Column(nullable = false)
    private int puntaje;

    /** Decisión clínica asociada al nivel (ej. "Mantener con ajustes"). */
    @Column(nullable = false, length = 60)
    private String decision;

    @Column(nullable = false)
    private LocalDate fecha;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;
}
