# ─────────────────────────────────────────────
# Stage 1: Build  (Maven + JDK 21 já incluídos)
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copia pom.xml primeiro — dependências só são rebaixadas se o pom.xml mudar
COPY pom.xml .

# Pré-resolve dependências para cachear esta layer separada do código
RUN mvn dependency:resolve -B -q

COPY src/ src/

# Build sem testes (testes rodam no CI)
RUN mvn package -DskipTests -B -q

# ─────────────────────────────────────────────
# Stage 2: Runtime (só JRE, imagem mínima)
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Cria pasta de uploads e usuário sem privilégios (boa prática de segurança)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/uploads && chown -R appuser:appgroup /app

USER appuser
WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# JVM tunada para container:
# -XX:+UseContainerSupport  → respeita limits do Docker
# -XX:MaxRAMPercentage=75   → usa até 75% da RAM do container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
