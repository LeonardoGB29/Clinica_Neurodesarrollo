package pe.sanjuandedios.clinica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Paciente del Centro de Neurodesarrollo. */
@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @Column(length = 40)
    private String id;

    @Column(name = "nro_historia", nullable = false, unique = true, length = 30)
    private String nroHistoria;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 4)
    private String iniciales;

    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNac;

    /** "M" o "F". */
    @Column(nullable = false, length = 1)
    private String sexo;

    /** Diagnósticos con código CIE-11, almacenados como arreglo nativo de PostgreSQL (text[]). */
    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> diagnosticos = new ArrayList<>();

    @Column(name = "apoderado_nombre", length = 120)
    private String apoderadoNombre;

    @Column(name = "apoderado_relacion", length = 40)
    private String apoderadoRelacion;

    @Column(name = "apoderado_telefono", length = 30)
    private String apoderadoTelefono;

    @Column(length = 120)
    private String cobertura;

    /** Institución educativa, ej. "I.E. Santa Rosa · 1.er grado". */
    @Column(length = 120)
    private String colegio;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;
}
