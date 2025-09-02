#!/bin/bash

# Script de Verificación de Seguridad para MS Distribution
# Verifica que todas las medidas de seguridad estén funcionando correctamente

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
BASE_URL="http://localhost:8086"
API_ENDPOINT="/api/v2/programs"
HEALTH_ENDPOINT="/actuator/health"

echo -e "${BLUE}🔒 Verificación de Seguridad - MS Distribution${NC}"
echo "=================================================="
echo ""

# Función para verificar si el servicio está corriendo
check_service_running() {
    echo -e "${BLUE}1. Verificando si el servicio está corriendo...${NC}"
    
    if curl -s "$BASE_URL$HEALTH_ENDPOINT" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Servicio corriendo en $BASE_URL${NC}"
    else
        echo -e "${RED}❌ Servicio no está corriendo en $BASE_URL${NC}"
        echo "   Ejecuta: mvn spring-boot:run -Dspring-boot.run.profiles=dev"
        exit 1
    fi
    echo ""
}

# Función para verificar headers de seguridad
check_security_headers() {
    echo -e "${BLUE}2. Verificando headers de seguridad...${NC}"
    
    local headers=$(curl -s -I "$BASE_URL$API_ENDPOINT")
    
    # Verificar headers específicos
    local header_checks=(
        "X-Content-Type-Options: nosniff"
        "X-Frame-Options: DENY"
        "X-XSS-Protection: 1; mode=block"
        "Referrer-Policy: strict-origin-when-cross-origin"
        "Permissions-Policy: geolocation=(), microphone=(), camera=()"
    )
    
    local all_headers_present=true
    
    for header in "${header_checks[@]}"; do
        if echo "$headers" | grep -q "$header"; then
            echo -e "${GREEN}✅ $header${NC}"
        else
            echo -e "${RED}❌ $header (faltante)${NC}"
            all_headers_present=false
        fi
    done
    
    # Verificar que headers sensibles NO estén presentes
    local sensitive_headers=(
        "Server:"
        "X-Powered-By:"
        "X-AspNet-Version:"
        "X-AspNetMvc-Version:"
    )
    
    for header in "${sensitive_headers[@]}"; do
        if echo "$headers" | grep -q "$header"; then
            echo -e "${RED}❌ $header (presente - información expuesta)${NC}"
            all_headers_present=false
        else
            echo -e "${GREEN}✅ $header (no expuesto)${NC}"
        fi
    done
    
    if [ "$all_headers_present" = true ]; then
        echo -e "${GREEN}✅ Todos los headers de seguridad están configurados correctamente${NC}"
    else
        echo -e "${RED}❌ Algunos headers de seguridad están faltando o mal configurados${NC}"
    fi
    echo ""
}

# Función para verificar endpoints sensibles bloqueados
check_sensitive_endpoints() {
    echo -e "${BLUE}3. Verificando endpoints sensibles bloqueados...${NC}"
    
    local sensitive_endpoints=(
        "/actuator/env"
        "/actuator/configprops"
        "/actuator/beans"
        "/actuator/metrics"
        "/actuator/prometheus"
        "/error"
    )
    
    local all_blocked=true
    
    for endpoint in "${sensitive_endpoints[@]}"; do
        local response=$(curl -s -w "%{http_code}" "$BASE_URL$endpoint" -o /dev/null)
        
        if [ "$response" = "403" ] || [ "$response" = "404" ]; then
            echo -e "${GREEN}✅ $endpoint bloqueado (HTTP $response)${NC}"
        else
            echo -e "${RED}❌ $endpoint accesible (HTTP $response)${NC}"
            all_blocked=false
        fi
    done
    
    if [ "$all_blocked" = true ]; then
        echo -e "${GREEN}✅ Todos los endpoints sensibles están bloqueados${NC}"
    else
        echo -e "${RED}❌ Algunos endpoints sensibles están accesibles${NC}"
    fi
    echo ""
}

# Función para verificar rate limiting
check_rate_limiting() {
    echo -e "${BLUE}4. Verificando rate limiting...${NC}"
    
    echo "   Enviando 101 requests rápidamente..."
    
    local rate_limited=false
    for i in {1..101}; do
        local response=$(curl -s -w "%{http_code}" "$BASE_URL$API_ENDPOINT" -o /dev/null)
        
        if [ "$response" = "429" ]; then
            rate_limited=true
            echo -e "${GREEN}✅ Rate limiting activado en request #$i (HTTP 429)${NC}"
            break
        fi
        
        # Mostrar progreso cada 20 requests
        if [ $((i % 20)) -eq 0 ]; then
            echo "   Progreso: $i/101 requests enviados..."
        fi
    done
    
    if [ "$rate_limited" = true ]; then
        echo -e "${GREEN}✅ Rate limiting está funcionando correctamente${NC}"
    else
        echo -e "${YELLOW}⚠️  Rate limiting no se activó después de 101 requests${NC}"
        echo "   Esto podría indicar que el rate limiting no está configurado o es muy permisivo"
    fi
    echo ""
}

# Función para verificar CORS
check_cors() {
    echo -e "${BLUE}5. Verificando configuración CORS...${NC}"
    
    # Test con origen permitido
    local cors_response=$(curl -s -H "Origin: http://localhost:3000" \
        -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: X-Requested-With" \
        -X OPTIONS \
        "$BASE_URL$API_ENDPOINT")
    
    if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then
        echo -e "${GREEN}✅ CORS configurado para origen localhost:3000${NC}"
    else
        echo -e "${RED}❌ CORS no configurado correctamente${NC}"
    fi
    
    # Test con origen no permitido
    local forbidden_origin_response=$(curl -s -H "Origin: https://malicious-site.com" \
        -H "Access-Control-Request-Method: GET" \
        -X OPTIONS \
        "$BASE_URL$API_ENDPOINT")
    
    if echo "$forbidden_origin_response" | grep -q "Access-Control-Allow-Origin"; then
        echo -e "${RED}❌ CORS permite origen malicioso (mal configurado)${NC}"
    else
        echo -e "${GREEN}✅ CORS bloquea origen malicioso correctamente${NC}"
    fi
    echo ""
}

# Función para verificar configuración de Swagger
check_swagger_config() {
    echo -e "${BLUE}6. Verificando configuración de Swagger...${NC}"
    
    local swagger_response=$(curl -s -w "%{http_code}" "$BASE_URL/swagger-ui.html" -o /dev/null)
    
    if [ "$response" = "200" ]; then
        echo -e "${YELLOW}⚠️  Swagger UI accesible (solo debe estar habilitado en desarrollo)${NC}"
        echo "   En producción, esto debe estar deshabilitado"
    else
        echo -e "${GREEN}✅ Swagger UI no accesible (HTTP $swagger_response)${NC}"
    fi
    
    local api_docs_response=$(curl -s -w "%{http_code}" "$BASE_URL/v3/api-docs" -o /dev/null)
    
    if [ "$api_docs_response" = "200" ]; then
        echo -e "${YELLOW}⚠️  API Docs accesible (solo debe estar habilitado en desarrollo)${NC}"
    else
        echo -e "${GREEN}✅ API Docs no accesible (HTTP $api_docs_response)${NC}"
    fi
    echo ""
}

# Función para verificar logging
check_logging() {
    echo -e "${BLUE}7. Verificando configuración de logging...${NC}"
    
    if [ -d "logs" ]; then
        echo -e "${GREEN}✅ Directorio de logs creado${NC}"
        
        # Verificar permisos del directorio de logs
        local log_perms=$(ls -ld logs | awk '{print $1}')
        if [[ "$log_perms" == drwxr-xr-x ]]; then
            echo -e "${GREEN}✅ Permisos de logs correctos: $log_perms${NC}"
        else
            echo -e "${YELLOW}⚠️  Permisos de logs inusuales: $log_perms${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Directorio de logs no encontrado${NC}"
        echo "   Se creará automáticamente cuando la aplicación genere logs"
    fi
    echo ""
}

# Función para resumen final
show_summary() {
    echo -e "${BLUE}📊 Resumen de Verificación de Seguridad${NC}"
    echo "=============================================="
    echo ""
    echo "Este script ha verificado:"
    echo "✅ Headers de seguridad HTTP"
    echo "✅ Bloqueo de endpoints sensibles"
    echo "✅ Rate limiting"
    echo "✅ Configuración CORS"
    echo "✅ Configuración de Swagger"
    echo "✅ Configuración de logging"
    echo ""
    echo -e "${GREEN}🎉 Verificación de seguridad completada!${NC}"
    echo ""
    echo "Para más información, consulta:"
    echo "📖 SECURITY_README.md - Documentación completa de seguridad"
    echo "🔧 application-dev.yml - Configuración de desarrollo"
    echo "🚀 application-prod.yml - Configuración de producción"
}

# Función principal
main() {
    check_service_running
    check_security_headers
    check_sensitive_endpoints
    check_rate_limiting
    check_cors
    check_swagger_config
    check_logging
    show_summary
}

# Ejecutar script
main "$@"
