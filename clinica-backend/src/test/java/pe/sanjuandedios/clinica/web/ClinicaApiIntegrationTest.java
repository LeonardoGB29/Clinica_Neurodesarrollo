package pe.sanjuandedios.clinica.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prueba de integración de extremo a extremo contra PostgreSQL 16 real (Testcontainers):
 * migraciones Flyway V1 + V2, seguridad JWT y el contrato API con los datos semilla.
 * Se omite automáticamente si Docker no está disponible.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClinicaApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String PACIENTE = "pac-0231";
    private static final String CICLO_TO = "cic-2026-TO-0231-03";
    private static String token;

    @Autowired
    TestRestTemplate rest;

    // ------------------------------------------------------------------ autenticación

    @Test
    @Order(1)
    void loginDevuelveUnJwtConLosDatosDelUsuario() {
        ResponseEntity<JsonNode> respuesta = rest.postForEntity("/api/v1/auth/login",
                Map.of("usuario", "mquispe", "clave", "clinica2026"), JsonNode.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        token = respuesta.getBody().get("token").asText();
        assertThat(token).isNotBlank();
        assertThat(respuesta.getBody().get("tipo").asText()).isEqualTo("Bearer");
        assertThat(respuesta.getBody().at("/usuario/profesionalId").asText()).isEqualTo("prof-017");
        assertThat(respuesta.getBody().at("/usuario/specialtyId").asText()).isEqualTo("TO");
    }

    @Test
    @Order(2)
    void credencialesInvalidasResponden401() {
        ResponseEntity<JsonNode> respuesta = rest.postForEntity("/api/v1/auth/login",
                Map.of("usuario", "mquispe", "clave", "incorrecta"), JsonNode.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respuesta.getBody().get("detail").asText()).contains("incorrectos");
    }

    @Test
    @Order(3)
    void sinTokenLaApiResponde401() {
        ResponseEntity<JsonNode> respuesta = rest.getForEntity("/api/v1/profesionales/me", JsonNode.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respuesta.getBody().get("title").asText()).isEqualTo("No autenticado");
    }

    // ------------------------------------------------------------------ lecturas con la forma de mockData.js

    @Test
    @Order(10)
    void profesionalMeTieneLaFormaDeCurrentProfessional() {
        JsonNode me = get("/api/v1/profesionales/me").getBody();

        assertThat(me.get("id").asText()).isEqualTo("prof-017");
        assertThat(me.get("fullName").asText()).isEqualTo("Lic. María Fernanda Quispe Rojas");
        assertThat(me.get("initials").asText()).isEqualTo("MQ");
        assertThat(me.get("specialtyId").asText()).isEqualTo("TO");
        assertThat(me.get("college").asText()).isEqualTo("CTMP");
        assertThat(me.get("licenseNumber").asText()).isEqualTo("14582");
        assertThat(me.get("email").asText()).isEqualTo("mquispe@clinicasanjuandedios.pe");
    }

    @Test
    @Order(11)
    void pacienteTieneLaFormaDePatient() {
        JsonNode paciente = get("/api/v1/pacientes/" + PACIENTE).getBody();

        assertThat(paciente.get("historyNumber").asText()).isEqualTo("HC-2024-0231");
        assertThat(paciente.get("fullName").asText()).isEqualTo("Mateo Alejandro Huamán Torres");
        assertThat(paciente.get("birthDate").asText()).isEqualTo("2019-03-12");
        assertThat(paciente.get("sex").asText()).isEqualTo("M");
        assertThat(paciente.get("diagnoses")).hasSize(2);
        assertThat(paciente.get("diagnoses").get(0).asText()).isEqualTo("TEA nivel 1 (CIE-11 6A02.0)");
        assertThat(paciente.at("/guardian/name").asText()).isEqualTo("Rosa Torres Villanueva");
        assertThat(paciente.at("/guardian/relation").asText()).isEqualTo("Madre");
        assertThat(paciente.get("coverage").asText()).isEqualTo("Salud Card · Convenio corporativo");
        assertThat(paciente.get("school").asText()).isEqualTo("I.E. Santa Rosa · 1.er grado");
    }

    @Test
    @Order(12)
    void pacienteInexistenteResponde404() {
        ResponseEntity<JsonNode> respuesta = get("/api/v1/pacientes/pac-9999");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody().get("title").asText()).isEqualTo("Recurso no encontrado");
    }

    @Test
    @Order(13)
    void cicloActivoTieneLaFormaDeCycleMasActiveObjectives() {
        JsonNode ciclo = get("/api/v1/pacientes/" + PACIENTE + "/ciclos?especialidad=TO").getBody();

        assertThat(ciclo.get("id").asText()).isEqualTo(CICLO_TO);
        assertThat(ciclo.get("specialtyId").asText()).isEqualTo("TO");
        assertThat(ciclo.get("startDate").asText()).isEqualTo("2026-09-24");
        assertThat(ciclo.get("totalSessions").asInt()).isEqualTo(12);
        assertThat(ciclo.get("currentSession").asInt()).isEqualTo(4);
        assertThat(ciclo.get("frequency").asText()).isEqualTo("1 sesión / semana · 45 min");
        assertThat(ciclo.get("maxObjectives").asInt()).isEqualTo(3);
        assertThat(ciclo.get("status").asText()).isEqualTo("open");

        JsonNode objetivos = ciclo.get("objectives");
        assertThat(objetivos).hasSize(2);
        assertThat(objetivos.get(0).get("id").asText()).isEqualTo("obj-1");
        assertThat(objetivos.get(0).get("bankId").asText()).isEqualTo("to-001");
        assertThat(objetivos.get(0).get("text").asText()).isEqualTo("Agarrar el lápiz con pinza trípode");
        assertThat(objetivos.get(0).get("baseline").asInt()).isEqualTo(10);
        assertThat(objetivos.get(0).get("target").asInt()).isEqualTo(80);
        assertThat(objetivos.get(0).get("addedInSession").asInt()).isEqualTo(1);
    }

    @Test
    @Order(14)
    void historialDeSesionesTieneLaFormaDeSessionHistory() {
        JsonNode sesiones = get("/api/v1/ciclos/" + CICLO_TO + "/sesiones").getBody();

        assertThat(sesiones).hasSize(3);
        JsonNode primera = sesiones.get(0);
        assertThat(primera.get("number").asInt()).isEqualTo(1);
        assertThat(primera.get("date").asText()).isEqualTo("2026-09-24");
        assertThat(primera.get("professionalName").asText()).isEqualTo("Lic. María Fernanda Quispe Rojas");
        assertThat(primera.at("/evaluations/obj-1/hits").asInt()).isEqualTo(2);
        assertThat(primera.at("/evaluations/obj-1/total").asInt()).isEqualTo(10);
        assertThat(primera.at("/evaluations/obj-2/hits").asInt()).isEqualTo(1);
        assertThat(primera.at("/evaluations/obj-2/total").asInt()).isEqualTo(5);
        assertThat(primera.get("activity").asText()).startsWith("Evaluación de línea base de la prensión.");
        assertThat(primera.get("achievement").asText()).startsWith("Logra pinza digital inferior.");
        assertThat(primera.get("observations").asText()).contains("Coordinar con Psicología");
    }

    @Test
    @Order(15)
    void progresoCalculaAcumuladoUltimaSesionVariacionYGas() {
        JsonNode progreso = get("/api/v1/ciclos/" + CICLO_TO + "/progreso").getBody();

        assertThat(progreso.get("cycleId").asText()).isEqualTo(CICLO_TO);
        assertThat(progreso.get("closing").asBoolean()).isFalse();
        JsonNode obj1 = progreso.get("objectives").get(0);
        assertThat(obj1.get("id").asText()).isEqualTo("obj-1");
        assertThat(obj1.get("accumulated").asInt()).isEqualTo(33);
        assertThat(obj1.get("lastScore").asInt()).isEqualTo(50);
        assertThat(obj1.get("lastDelta").asInt()).isEqualTo(20);
        assertThat(obj1.get("recordedCount").asInt()).isEqualTo(3);
        assertThat(obj1.get("series")).hasSize(3);
        assertThat(obj1.get("series").get(2).get("value").asInt()).isEqualTo(50);
        assertThat(obj1.at("/gas/level").asInt()).isEqualTo(-1);
        assertThat(obj1.at("/gas/decision").asText()).isEqualTo("Mantener con ajustes");
        assertThat(obj1.get("gasFinal").isNull()).isTrue();

        JsonNode obj2 = progreso.get("objectives").get(1);
        assertThat(obj2.get("accumulated").asInt()).isEqualTo(34);
        assertThat(obj2.get("lastDelta").asInt()).isEqualTo(17);
    }

    @Test
    @Order(16)
    void vista360TieneLaFormaDeOtherSpecialtiesProgress() {
        JsonNode vista = get("/api/v1/pacientes/" + PACIENTE + "/vista-360").getBody();

        assertThat(vista).hasSize(3);
        assertThat(vista).extracting(n -> n.get("specialtyId").asText()).containsExactly("TL", "TF", "PS");

        JsonNode tl = vista.get(0);
        assertThat(tl.get("professionalName").asText()).isEqualTo("Lic. Carla Mendoza Paredes");
        assertThat(tl.get("license").asText()).isEqualTo("CTMP 21877");
        assertThat(tl.get("currentSession").asInt()).isEqualTo(5);
        assertThat(tl.get("totalSessions").asInt()).isEqualTo(12);
        assertThat(tl.get("lastSessionDate").asText()).isEqualTo("2026-10-13");
        assertThat(tl.get("lastNote").asText()).startsWith("Responde mejor en formato de juego simbólico.");
        JsonNode objetivo = tl.get("objectives").get(0);
        assertThat(objetivo.get("id").asText()).isEqualTo("tl-obj-1");
        assertThat(objetivo.get("text").asText()).isEqualTo("Ampliar la longitud media del enunciado a 3-4 palabras");
        assertThat(objetivo.get("target").asInt()).isEqualTo(75);
        assertThat(objetivo.get("history")).extracting(JsonNode::asInt).containsExactly(20, 30, 40, 50, 55);

        JsonNode ps = vista.get(2);
        assertThat(ps.get("currentSession").asInt()).isEqualTo(6);
        assertThat(ps.get("objectives").get(2).get("history")).extracting(JsonNode::asInt)
                .containsExactly(15, 25, 35, 40, 50, 55);
    }

    @Test
    @Order(17)
    void notasCompartidasYEspecialidades() {
        JsonNode notas = get("/api/v1/pacientes/" + PACIENTE + "/notas").getBody();
        assertThat(notas).hasSize(3);
        assertThat(notas.get(0).get("id").asText()).isEqualTo("note-1");
        assertThat(notas.get(0).get("specialtyId").asText()).isEqualTo("PS");
        assertThat(notas.get(0).get("date").asText()).isEqualTo("2026-10-14");

        JsonNode especialidades = get("/api/v1/especialidades").getBody();
        assertThat(especialidades).hasSize(4);
        assertThat(especialidades.get(0).get("id").asText()).isEqualTo("TO");
        assertThat(especialidades.get(0).get("short").asText()).isEqualTo("T. Ocupacional");
        assertThat(especialidades.get(3).get("college").asText()).isEqualTo("CPsP");
    }

    @Test
    @Order(18)
    void bancoDeObjetivosDeLaEspecialidad() {
        JsonNode banco = get("/api/v1/banco-objetivos?especialidad=TO").getBody();

        assertThat(banco).hasSize(10);
        assertThat(banco.get(0).get("id").asText()).isEqualTo("to-001");
        assertThat(banco.get(0).get("text").asText()).isEqualTo("Agarrar el lápiz con pinza trípode");
        assertThat(get("/api/v1/banco-objetivos?especialidad=PS").getBody()).hasSize(7);
    }

    // ------------------------------------------------------------------ escrituras y reglas de negocio

    @Test
    @Order(20)
    void activaElTercerObjetivoYRechazaElCuartoCon409() {
        ResponseEntity<JsonNode> tercero = post("/api/v1/ciclos/" + CICLO_TO + "/objetivos",
                Map.of("bankId", "to-003", "baseline", 0, "target", 80));
        assertThat(tercero.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(tercero.getBody().get("id").asText()).isEqualTo("obj-101");
        assertThat(tercero.getBody().get("bankId").asText()).isEqualTo("to-003");
        assertThat(tercero.getBody().get("addedInSession").asInt()).isEqualTo(4);
        assertThat(tercero.getBody().get("order").asInt()).isEqualTo(3);

        ResponseEntity<JsonNode> cuarto = post("/api/v1/ciclos/" + CICLO_TO + "/objetivos",
                Map.of("bankId", "to-004", "baseline", 0, "target", 80));
        assertThat(cuarto.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(cuarto.getBody().get("detail").asText()).contains("máximo 3");

        ResponseEntity<JsonNode> duplicado = post("/api/v1/ciclos/" + CICLO_TO + "/objetivos",
                Map.of("bankId", "to-001"));
        assertThat(duplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        JsonNode ciclo = get("/api/v1/ciclos/" + CICLO_TO).getBody();
        assertThat(ciclo.get("objectives")).hasSize(3);
    }

    @Test
    @Order(21)
    void ajustaLineaBaseYMetaYValidaRangos() {
        ResponseEntity<JsonNode> ok = patch("/api/v1/ciclos/" + CICLO_TO + "/objetivos/obj-101", Map.of("baseline", 5, "target", 75));
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody().get("baseline").asInt()).isEqualTo(5);
        assertThat(ok.getBody().get("target").asInt()).isEqualTo(75);

        ResponseEntity<JsonNode> invalido = patch("/api/v1/ciclos/" + CICLO_TO + "/objetivos/obj-101", Map.of("target", 5));
        assertThat(invalido.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<JsonNode> fueraDeRango = patch("/api/v1/ciclos/" + CICLO_TO + "/objetivos/obj-101", Map.of("target", 150));
        assertThat(fueraDeRango.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(fueraDeRango.getBody().get("errores").has("target")).isTrue();
    }

    @Test
    @Order(22)
    void otraEspecialidadNoPuedeModificarElCiclo() {
        String tokenTl = rest.postForEntity("/api/v1/auth/login",
                Map.of("usuario", "cmendoza", "clave", "clinica2026"), JsonNode.class).getBody().get("token").asText();
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setBearerAuth(tokenTl);
        cabeceras.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<JsonNode> respuesta = rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/objetivos/obj-101",
                HttpMethod.DELETE, new HttpEntity<>(cabeceras), JsonNode.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        // Pero sí puede leerlo (vista interdisciplinaria)
        assertThat(rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/sesiones", HttpMethod.GET,
                new HttpEntity<>(cabeceras), JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(23)
    void registraLaSesionEnCursoYAvanzaElCiclo() {
        Map<String, Object> cuerpo = Map.of(
                "number", 4,
                "date", "2026-10-15",
                "evaluations", Map.of(
                        "obj-1", Map.of("hits", 6, "total", 10),
                        "obj-2", Map.of("hits", 4, "total", 6),
                        "obj-101", Map.of("hits", 1, "total", 10)),
                "activity", "Trazos con lápiz adaptado y abotonado en tablero.",
                "achievement", "Pinza trípode en 6 de 10 intentos.",
                "observations", "Continuar con el adaptador en casa.");

        ResponseEntity<JsonNode> creada = post("/api/v1/ciclos/" + CICLO_TO + "/sesiones", cuerpo);
        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creada.getBody().get("number").asInt()).isEqualTo(4);
        assertThat(creada.getBody().get("professionalName").asText()).isEqualTo("Lic. María Fernanda Quispe Rojas");
        assertThat(creada.getBody().at("/evaluations/obj-1/hits").asInt()).isEqualTo(6);
        assertThat(creada.getBody().at("/evaluations/obj-101/total").asInt()).isEqualTo(10);

        assertThat(get("/api/v1/ciclos/" + CICLO_TO + "/sesiones").getBody()).hasSize(4);
        JsonNode ciclo = get("/api/v1/pacientes/" + PACIENTE + "/ciclos?especialidad=TO").getBody();
        assertThat(ciclo.get("currentSession").asInt()).isEqualTo(5);
        assertThat(ciclo.get("status").asText()).isEqualTo("open");

        JsonNode progreso = get("/api/v1/ciclos/" + CICLO_TO + "/progreso").getBody();
        JsonNode obj1 = progreso.get("objectives").get(0);
        assertThat(obj1.get("lastScore").asInt()).isEqualTo(60);
        assertThat(obj1.get("lastDelta").asInt()).isEqualTo(10);
        assertThat(obj1.get("accumulated").asInt()).isEqualTo(40);
    }

    @Test
    @Order(24)
    void rechazaSesionesInvalidas() {
        ResponseEntity<JsonNode> aciertosMayores = post("/api/v1/ciclos/" + CICLO_TO + "/sesiones", Map.of(
                "evaluations", Map.of("obj-1", Map.of("hits", 11, "total", 10)),
                "activity", "x", "achievement", "y"));
        assertThat(aciertosMayores.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(aciertosMayores.getBody().get("detail").asText()).contains("aciertos");

        ResponseEntity<JsonNode> sinTextos = post("/api/v1/ciclos/" + CICLO_TO + "/sesiones", Map.of(
                "evaluations", Map.of("obj-1", Map.of("hits", 5, "total", 10)),
                "activity", "", "achievement", ""));
        assertThat(sinTextos.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(sinTextos.getBody().get("errores").has("activity")).isTrue();

        ResponseEntity<JsonNode> numeroIncorrecto = post("/api/v1/ciclos/" + CICLO_TO + "/sesiones", Map.of(
                "number", 9,
                "evaluations", Map.of("obj-1", Map.of("hits", 5, "total", 10)),
                "activity", "x", "achievement", "y"));
        assertThat(numeroIncorrecto.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(25)
    void lasSesionesGuardadasSonInmutables() {
        HttpEntity<Object> cuerpo = new HttpEntity<>(Map.of("activity", "cambio"), auth());

        assertThat(rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/sesiones/4", HttpMethod.PUT, cuerpo, JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/sesiones/4", HttpMethod.PATCH, cuerpo, JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/sesiones/4", HttpMethod.DELETE, new HttpEntity<>(auth()), JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(get("/api/v1/ciclos/" + CICLO_TO + "/sesiones/4").getBody().get("number").asInt()).isEqualTo(4);
    }

    @Test
    @Order(26)
    void retiraUnObjetivoYLiberaElCupo() {
        ResponseEntity<JsonNode> retirado = rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/objetivos/obj-101",
                HttpMethod.DELETE, new HttpEntity<>(auth()), JsonNode.class);
        assertThat(retirado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get("/api/v1/ciclos/" + CICLO_TO).getBody().get("objectives")).hasSize(2);

        ResponseEntity<JsonNode> nuevo = post("/api/v1/ciclos/" + CICLO_TO + "/objetivos", Map.of("bankId", "to-004"));
        assertThat(nuevo.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(nuevo.getBody().get("order").asInt()).isEqualTo(3);
        assertThat(nuevo.getBody().get("addedInSession").asInt()).isEqualTo(5);
    }

    @Test
    @Order(27)
    void creaObjetivosEnElBancoSinDuplicar() {
        ResponseEntity<JsonNode> creado = post("/api/v1/banco-objetivos",
                Map.of("text", "  Apilar  6 cubos con   coordinación bimanual "));
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getBody().get("id").asText()).startsWith("to-");
        assertThat(creado.getBody().get("specialtyId").asText()).isEqualTo("TO");
        assertThat(creado.getBody().get("text").asText()).isEqualTo("Apilar 6 cubos con coordinación bimanual");

        ResponseEntity<JsonNode> repetido = post("/api/v1/banco-objetivos",
                Map.of("text", "apilar 6 cubos con coordinación bimanual"));
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(repetido.getBody().get("id").asText()).isEqualTo(creado.getBody().get("id").asText());
        assertThat(get("/api/v1/banco-objetivos?especialidad=TO").getBody()).hasSize(11);
    }

    @Test
    @Order(28)
    void cierraUnCicloYPersisteElGasDefinitivo() {
        // El ciclo de TF lo cierra la coordinación (cierre anticipado)
        String tokenCoordinador = rest.postForEntity("/api/v1/auth/login",
                Map.of("usuario", "coordinador", "clave", "clinica2026"), JsonNode.class).getBody().get("token").asText();
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setBearerAuth(tokenCoordinador);

        ResponseEntity<JsonNode> cierre = rest.exchange("/api/v1/ciclos/cic-2026-TF-0231-01/cierre", HttpMethod.POST,
                new HttpEntity<>(cabeceras), JsonNode.class);

        assertThat(cierre.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cierre.getBody().get("status").asText()).isEqualTo("closed");
        assertThat(cierre.getBody().get("closing").asBoolean()).isTrue();
        JsonNode tf1 = cierre.getBody().get("objectives").get(0);
        // tf-obj-1: último 40 %, base 20, meta 80 → 40 > 20 → -1
        assertThat(tf1.at("/gasFinal/level").asInt()).isEqualTo(-1);
        assertThat(tf1.at("/gasFinal/score").asInt()).isEqualTo(40);
        assertThat(tf1.at("/gasFinal/decision").asText()).isEqualTo("Mantener con ajustes");

        // Cerrado: ya no admite sesiones ni aparece como ciclo activo
        ResponseEntity<JsonNode> sesion = rest.exchange("/api/v1/ciclos/cic-2026-TF-0231-01/sesiones", HttpMethod.POST,
                new HttpEntity<>(Map.of("evaluations", Map.of("tf-obj-1", Map.of("hits", 5, "total", 10)),
                        "activity", "x", "achievement", "y"), cabeceras), JsonNode.class);
        assertThat(sesion.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(get("/api/v1/pacientes/" + PACIENTE + "/ciclos?especialidad=TF").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/v1/pacientes/" + PACIENTE + "/vista-360").getBody()).hasSize(2);
    }

    // ------------------------------------------------------------------ reporte y documentación

    @Test
    @Order(30)
    void generaElReportePdf() {
        HttpHeaders cabeceras = auth();
        cabeceras.setAccept(List.of(MediaType.APPLICATION_PDF));
        ResponseEntity<byte[]> respuesta = rest.exchange("/api/v1/ciclos/" + CICLO_TO + "/reporte.pdf", HttpMethod.GET,
                new HttpEntity<>(cabeceras), byte[].class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(respuesta.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("ficha-evolucion-" + CICLO_TO);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(new String(respuesta.getBody(), 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @Order(31)
    void laDocumentacionOpenApiEsPublica() {
        ResponseEntity<String> docs = rest.getForEntity("/v3/api-docs", String.class);
        assertThat(docs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(docs.getBody()).contains("/api/v1/ciclos/{id}/progreso", "/api/v1/pacientes/{id}/vista-360", "bearerAuth");

        ResponseEntity<String> swagger = rest.getForEntity("/swagger-ui.html", String.class);
        assertThat(swagger.getStatusCode().is2xxSuccessful() || swagger.getStatusCode().is3xxRedirection()).isTrue();

        assertThat(rest.getForEntity("/actuator/health", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ------------------------------------------------------------------ utilidades

    private HttpHeaders auth() {
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setBearerAuth(token);
        cabeceras.setContentType(MediaType.APPLICATION_JSON);
        cabeceras.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON));
        return cabeceras;
    }

    private ResponseEntity<JsonNode> get(String url) {
        return rest.exchange(url, HttpMethod.GET, new HttpEntity<>(auth()), JsonNode.class);
    }

    private ResponseEntity<JsonNode> post(String url, Object cuerpo) {
        return rest.exchange(url, HttpMethod.POST, new HttpEntity<>(cuerpo, auth()), JsonNode.class);
    }

    private ResponseEntity<JsonNode> patch(String url, Object cuerpo) {
        return rest.exchange(url, HttpMethod.PATCH, new HttpEntity<>(cuerpo, auth()), JsonNode.class);
    }
}
