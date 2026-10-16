package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Especialidad del centro (TO, TL, TF, PS) con su colegio profesional y color en la UI. */
@Entity
@Table(name = "especialidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Especialidad {

    @Id
    @Column(length = 4)
    private String id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "nombre_corto", nullable = false, length = 40)
    private String nombreCorto;

    @Column(name = "colegio_profesional", nullable = false, length = 20)
    private String colegioProfesional;

    @Column(nullable = false, length = 7)
    private String color;

    /** Orden de presentación en la interfaz (TO, TL, TF, PS). */
    @Column(nullable = false)
    private int orden;
}
