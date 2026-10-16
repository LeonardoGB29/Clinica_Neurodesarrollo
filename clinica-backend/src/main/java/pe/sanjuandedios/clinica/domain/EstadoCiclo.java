package pe.sanjuandedios.clinica.domain;

/** Estado del ciclo terapéutico. En la API se expone como "open" / "closed" (forma que espera el front). */
public enum EstadoCiclo {
    ABIERTO("open"),
    CERRADO("closed");

    private final String api;

    EstadoCiclo(String api) {
        this.api = api;
    }

    /** Valor usado en los DTOs: "open" o "closed". */
    public String toApi() {
        return api;
    }
}
