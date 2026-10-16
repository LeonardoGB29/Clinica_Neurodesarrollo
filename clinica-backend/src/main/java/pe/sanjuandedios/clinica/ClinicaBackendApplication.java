package pe.sanjuandedios.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la API REST del Sistema de Gestión Clínica Interdisciplinaria
 * (Centro de Neurodesarrollo TEA/TDAH – Clínica San Juan de Dios).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClinicaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicaBackendApplication.class, args);
    }
}
