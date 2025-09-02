# =========================
# Etapa 1: Build seguro
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copiar pom.xml explícitamente
COPY ./pom.xml /app/pom.xml
RUN mvn dependency:go-offline -B

# Copiar el resto del código
COPY ./src /app/src

# Compilar el JAR
RUN mvn clean package -DskipTests

# =========================
# Etapa 2: Producción segura
# =========================
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appuser && adduser -S appuser -G appuser
RUN mkdir -p /app/logs && chown -R appuser:appuser /app
USER appuser

WORKDIR /app

EXPOSE 8086

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

COPY --from=builder --chown=appuser:appuser /app/target/*.jar /app/app.jar

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
