package pe.sanjuandedios.clinica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parámetros del JWT ({@code app.security.jwt.*}).
 *
 * @param secret            clave HMAC de al menos 32 bytes (HS256)
 * @param expirationMinutes vigencia del token en minutos
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record SeguridadProperties(String secret, long expirationMinutes) {}
