# syntax=docker/dockerfile:1

# ---------- Build ----------
FROM eclipse-temurin:11-jdk-jammy AS build
WORKDIR /workspace

# Dependencias primeiro: so sao baixadas de novo quando o pom.xml muda
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Os testes rodam no CI / localmente (./mvnw test); a imagem so empacota
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q package -DskipTests \
 && java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ---------- Runtime ----------
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

# Camadas do Spring Boot: dependencias (mudam pouco) antes do codigo da aplicacao
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

USER app

ENV TZ=America/Sao_Paulo \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
