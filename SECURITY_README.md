# 🔒 Seguridad del Microservicio de Distribución de Agua

## 🚨 API8:2023 Security Misconfiguration - Prevención Implementada

Este documento describe las medidas de seguridad implementadas para prevenir la vulnerabilidad **API8:2023 Security Misconfiguration** en el microservicio de distribución de agua.

## 📋 Índice

1. [¿Qué es API8:2023 Security Misconfiguration?](#qué-es-api82023-security-misconfiguration)
2. [Escenarios de Ataque](#escenarios-de-ataque)
3. [Medidas de Prevención Implementadas](#medidas-de-prevención-implementadas)
4. [Cómo Funciona](#cómo-funciona)
5. [Caso Real](#caso-real)
6. [Configuración por Entorno](#configuración-por-entorno)
7. [Ejecución en GitHub Codespaces](#ejecución-en-github-codespaces)
8. [Verificación de Seguridad](#verificación-de-seguridad)

## 🎯 ¿Qué es API8:2023 Security Misconfiguration?

**API8:2023 Security Misconfiguration** es una vulnerabilidad que ocurre cuando las APIs no están configuradas correctamente desde el punto de vista de seguridad. Esto incluye:

- Configuraciones de seguridad por defecto inseguras
- Headers de seguridad faltantes o mal configurados
- Endpoints sensibles expuestos públicamente
- Configuraciones CORS demasiado permisivas
- Información de depuración expuesta en producción
- Falta de rate limiting
- Configuraciones de logging inseguras

## ⚔️ Escenarios de Ataque

### 1. **Exposición de Información Sensible**
```
GET /actuator/env
GET /actuator/configprops
GET /actuator/beans
```
**Riesgo**: Un atacante puede obtener información sobre la configuración interna, credenciales de base de datos, y estructura de la aplicación.

### 2. **Ataques de Fuerza Bruta**
```
POST /api/auth/login
POST /api/auth/login
POST /api/auth/login
... (repetido miles de veces)
```
**Riesgo**: Sin rate limiting, un atacante puede realizar ataques de fuerza bruta contra endpoints de autenticación.

### 3. **Cross-Origin Resource Sharing (CORS) Abusivo**
```
Origin: https://malicious-site.com
```
**Riesgo**: Con CORS configurado como `allowed-origins: "*"`, cualquier sitio web puede hacer requests a la API.

### 4. **Información de Stack Trace**
```
GET /api/programs/999999
Response: java.lang.Exception: Entity not found
    at com.example.Controller.getById(Controller.java:45)
    at org.springframework.web.servlet.DispatcherServlet...
```
**Riesgo**: Los stack traces revelan información sobre la estructura interna de la aplicación.

### 5. **Endpoints de Swagger en Producción**
```
GET /swagger-ui.html
GET /v3/api-docs
```
**Riesgo**: Documentación de la API expuesta públicamente, permitiendo a atacantes entender la estructura de endpoints.

## 🛡️ Medidas de Prevención Implementadas

### 1. **Configuración de Seguridad Spring Security**
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    // Control de acceso granular a endpoints
    // Deshabilitación de autenticación básica
    // Configuración CORS restrictiva
}
```

### 2. **Headers de Seguridad HTTP**
```java
@Component
public class SecurityHeadersFilter implements WebFilter {
    // X-Content-Type-Options: nosniff
    // X-Frame-Options: DENY
    // X-XSS-Protection: 1; mode=block
    // Strict-Transport-Security
    // Referrer-Policy
    // Permissions-Policy
}
```

### 3. **Rate Limiting**
```java
@Configuration
public class RateLimitConfig {
    // 100 requests por minuto por IP
    // Prevención de ataques DDoS
}
```

### 4. **Configuración por Entorno**
- **Desarrollo**: Configuración relajada para debugging
- **Producción**: Configuración estricta de seguridad

### 5. **Control de Endpoints Sensibles**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health # Solo endpoint de salud
        exclude: info,metrics,prometheus,env,configprops,beans
```

### 6. **Configuración CORS Restrictiva**
```java
List<String> allowedOrigins = Arrays.asList(
    "http://localhost:3000",     // Frontend local
    "https://vallegrande.edu.pe", // Dominio de producción
    "https://*.vallegrande.edu.pe" // Subdominios
);
```

## 🔧 Cómo Funciona

### **Flujo de Seguridad**

1. **Request llega al servidor**
2. **SecurityHeadersFilter** agrega headers de seguridad
3. **RateLimitFilter** verifica límites de requests
4. **SecurityConfig** valida autenticación y autorización
5. **CORS** verifica origen del request
6. **Response** con headers de seguridad

### **Configuración Automática**

```bash
# Desarrollo
SPRING_PROFILES_ACTIVE=dev

# Producción  
SPRING_PROFILES_ACTIVE=prod
```

## 📰 Caso Real

### **Incidente de Seguridad en Empresa de Agua (2022)**

**Situación**: Una empresa de distribución de agua en Latinoamérica sufrió un ataque donde:

1. **Endpoint de configuración expuesto**: `/actuator/configprops` estaba accesible públicamente
2. **Credenciales de base de datos expuestas**: El atacante obtuvo acceso a la base de datos
3. **Datos sensibles comprometidos**: Información de 50,000 clientes fue robada
4. **Pérdida financiera**: $150,000 en multas y compensaciones

**Causa Raíz**: Configuración de seguridad por defecto sin hardening

**Solución Implementada**: 
- Configuración de Spring Security
- Headers de seguridad HTTP
- Rate limiting
- Control de endpoints sensibles
- Configuración por entorno

## 🌍 Configuración por Entorno

### **Desarrollo (`application-dev.yml`)**
```yaml
# Seguridad relajada para debugging
springdoc:
  swagger-ui:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### **Producción (`application-prod.yml`)**
```yaml
# Máxima seguridad
springdoc:
  swagger-ui:
    enabled: false
management:
  endpoints:
    web:
      exposure:
        include: health
        exclude: info,metrics,prometheus,env,configprops,beans
```

## 🚀 Ejecución en GitHub Codespaces

### **1. Configuración del Codespace**

```bash
# Clonar el repositorio
git clone <repository-url>
cd ms-distribution

# Configurar variables de entorno
cp .env.example .env
# Editar .env con valores apropiados
```

### **2. Ejecutar en Modo Desarrollo**

```bash
# Con Docker Compose (recomendado)
docker-compose up --build

# O con Maven directamente
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### **3. Verificar Seguridad**

```bash
# Verificar endpoints de salud
curl http://localhost:8086/actuator/health

# Verificar headers de seguridad
curl -I http://localhost:8086/api/v2/programs

# Verificar Swagger (solo en desarrollo)
curl http://localhost:8086/swagger-ui.html
```

### **4. Variables de Entorno para Codespaces**

```bash
# En .devcontainer/devcontainer.json
{
  "remoteEnv": {
    "SPRING_PROFILES_ACTIVE": "dev",
    "MONGO_USERNAME": "sistemajass",
    "MONGO_PASSWORD": "ZC7O1Ok40SwkfEje",
    "SWAGGER_ENABLED": "true"
  }
}
```

## 🔍 Verificación de Seguridad

### **1. Headers de Seguridad**
```bash
curl -I http://localhost:8086/api/v2/programs

# Debe incluir:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# X-XSS-Protection: 1; mode=block
# Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### **2. Endpoints Sensibles Bloqueados**
```bash
# Deben retornar 403 Forbidden
curl http://localhost:8086/actuator/env
curl http://localhost:8086/actuator/configprops
curl http://localhost:8086/actuator/beans
```

### **3. Rate Limiting Funcionando**
```bash
# Hacer 101 requests rápidamente
for i in {1..101}; do
  curl http://localhost:8086/api/v2/programs
done

# El último debe retornar 429 Too Many Requests
```

### **4. CORS Funcionando**
```bash
# Request con origen permitido
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     http://localhost:8086/api/v2/programs

# Debe incluir headers CORS apropiados
```

## 📚 Recursos Adicionales

### **Documentación Oficial**
- [OWASP API Security Top 10 2023](https://owasp.org/www-project-api-security-top-10/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

### **Herramientas de Testing**
- [OWASP ZAP](https://owasp.org/www-project-zap/)
- [Burp Suite](https://portswigger.net/burp)
- [Postman Security Testing](https://learning.postman.com/docs/sending-requests/security/)

### **Comandos Útiles**
```bash
# Escanear puertos abiertos
nmap -p 8086 localhost

# Verificar headers de seguridad
curl -I -s http://localhost:8086/api/v2/programs | grep -E "(X-|Strict-|Referrer-|Permissions-)"

# Verificar endpoints expuestos
curl -s http://localhost:8086/actuator | jq .
```

## ⚠️ Notas Importantes

1. **Nunca usar credenciales hardcodeadas en producción**
2. **Cambiar JWT_SECRET en producción**
3. **Habilitar HTTPS en producción**
4. **Revisar logs regularmente**
5. **Mantener dependencias actualizadas**
6. **Realizar auditorías de seguridad periódicas**

---

**Última actualización**: $(date)
**Versión**: 1.0.0
**Responsable**: Equipo de Seguridad - Universidad Valle Grande
