package pe.sanjuandedios.clinica.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el JWT de la cabecera {@code Authorization: Bearer <token>} y, si es válido,
 * autentica la petición. Si falta o es inválido, la petición sigue anónima y el
 * {@code AuthenticationEntryPoint} responde 401 cuando la ruta lo exige.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.extraerUsuario(cabecera.substring(PREFIJO.length())).ifPresent(nombre -> autenticar(nombre, request));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String nombreUsuario, HttpServletRequest request) {
        try {
            UserDetails usuario = userDetailsService.loadUserByUsername(nombreUsuario);
            if (!usuario.isEnabled()) {
                return;
            }
            var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        } catch (UsernameNotFoundException e) {
            // El token referencia un usuario que ya no existe: la petición sigue anónima.
        }
    }
}
