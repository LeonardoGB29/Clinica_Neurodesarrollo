package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Profesional de salud del centro (terapeuta o psicólogo) con su colegiatura. */
@Entity
@Table(name = "profesional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profesional {

    @Id
    @Column(length = 40)
    private String id;

    /** Nombre completo con título, ej. "Lic. María Fernanda Quispe Rojas". */
    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 4)
    private String iniciales;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    /** Colegio profesional (CTMP, CPsP). */
    @Column(nullable = false, length = 20)
    private String colegio;

    @Column(name = "nro_colegiatura", nullable = false, length = 20)
    private String nroColegiatura;

    @Column(length = 120)
    private String correo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    /** Licencia legible para la vista 360°, ej. "CTMP 21877". */
    public String getLicencia() {
        return colegio + " " + nroColegiatura;
    }
}
