# ============================================================================
#  Imagen de la API (multi-stage): compila con Maven + JDK 21 y ejecuta con JRE 21.
# ============================================================================

# ---------- Etapa 1: construcción ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/clinica-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
