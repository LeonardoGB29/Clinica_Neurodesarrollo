package pe.sanjuandedios.clinica.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import pe.sanjuandedios.clinica.config.SeguridadProperties;

/** Emisión y validación de tokens JWT firmados con HS256 (jjwt). */
@Component
public class JwtService {

    private final SecretKey clave;
    private final Duration vigencia;

    public JwtService(SeguridadProperties props) {
        byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.security.jwt.secret debe tener al menos 32 bytes (HS256).");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.vigencia = Duration.ofMinutes(props.expirationMinutes());
    }

    /** Genera el token del usuario con sus datos básicos como claims. */
    public String generar(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now();
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.getUsername())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)));
        if (usuario.getProfesionalId() != null) {
            builder.claim("profesionalId", usuario.getProfesionalId());
        }
        if (usuario.getEspecialidadId() != null) {
            builder.claim("especialidadId", usuario.getEspecialidadId());
        }
        return builder.signWith(clave).compact();
    }

    /** Nombre de usuario (subject) si el token es válido y no ha expirado. */
    public Optional<String> extraerUsuario(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(subject);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long segundosDeVigencia() {
        return vigencia.toSeconds();
    }
}
