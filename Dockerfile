# ─────────────────────────────────────────────
# Stage 1: Build  (usa Maven + JDK 21, isolado)
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copia pom.xml primeiro para aproveitar o cache de layers do Docker:
# dependências só são rebaixadas se o pom.xml mudar
COPY pom.xml .
COPY .mvn/ .mvn/

# Instala Maven via apk (versão estável do repositório Alpine)
RUN apk add --no-cache maven

# Baixa dependências offline antes de copiar o código (melhor uso de cache)
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src/ src/

# Build sem testes (testes rodam no CI)
RUN mvn package -DskipTests -B --no-transfer-progress

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
