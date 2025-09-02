# Multi-stage build para mayor seguridad
FROM openjdk:17-jdk-slim AS builder

# Usuario no-root para mayor seguridad
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar archivos del proyecto
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Instalar dependencias y construir
RUN mvn clean package -DskipTests

# Imagen de producción
FROM openjdk:17-jre-slim

# Usuario no-root para mayor seguridad
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Crear directorio de logs
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Cambiar al usuario no-root
USER appuser

# Puerto de la aplicación
EXPOSE 8086

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Copiar JAR desde el builder
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8086/actuator/health || exit 1

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
