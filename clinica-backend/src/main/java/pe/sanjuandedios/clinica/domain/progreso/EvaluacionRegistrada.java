package pe.sanjuandedios.clinica.domain.progreso;

/** Evaluación de un objetivo en una sesión: aciertos sobre intentos (ej. 3 de 10 = 30 %). */
public record EvaluacionRegistrada(Integer aciertos, Integer intentos) {}
