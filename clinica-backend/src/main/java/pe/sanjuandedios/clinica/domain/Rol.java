package pe.sanjuandedios.clinica.domain;

/** Roles del sistema. Los terapeutas y psicólogos editan solo su especialidad; coordinación edita todo; sistemas solo lee. */
public enum Rol {
    TERAPEUTA,
    PSICOLOGO,
    COORDINADOR,
    SISTEMAS
}
