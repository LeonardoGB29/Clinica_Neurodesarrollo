package pe.sanjuandedios.clinica.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentación OpenAPI (Swagger UI en /swagger-ui.html). */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API – Sistema de Gestión Clínica Interdisciplinaria",
                version = "v1",
                description = """
                        Centro de Neurodesarrollo (TEA/TDAH) · Clínica San Juan de Dios.
                        Ficha de evolución, gestión de objetivos (máx. 3 por ciclo de 12 sesiones),
                        progreso con escala GAS y vista 360° interdisciplinaria.

                        Autenticación: obtener un token en **POST /api/v1/auth/login** y enviarlo como
                        `Authorization: Bearer <token>` (botón *Authorize*).
                        Usuarios de prueba: mquispe, cmendoza, jsalazar, acastillo, coordinador · clave `clinica2026`.
                        """,
                contact = @Contact(name = "Área de Sistemas – Clínica San Juan de Dios")),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {}
