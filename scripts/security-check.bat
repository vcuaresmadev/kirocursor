@echo off
REM Script de Verificación de Seguridad para MS Distribution (Windows)
REM Verifica que todas las medidas de seguridad estén funcionando correctamente

setlocal enabledelayedexpansion

REM Colores para output (Windows 10+)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Variables
set "BASE_URL=http://localhost:8086"
set "API_ENDPOINT=/api/v2/programs"
set "HEALTH_ENDPOINT=/actuator/health"

echo %BLUE%🔒 Verificación de Seguridad - MS Distribution%NC%
echo ==================================================
echo.

REM Función para verificar si el servicio está corriendo
echo %BLUE%1. Verificando si el servicio está corriendo...%NC%

curl -s "%BASE_URL%%HEALTH_ENDPOINT%" >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ Servicio corriendo en %BASE_URL%%NC%
) else (
    echo %RED%❌ Servicio no está corriendo en %BASE_URL%%NC%
    echo    Ejecuta: mvn spring-boot:run -Dspring-boot.run.profiles=dev
    exit /b 1
)
echo.

REM Función para verificar headers de seguridad
echo %BLUE%2. Verificando headers de seguridad...%NC%

curl -s -I "%BASE_URL%%API_ENDPOINT%" > temp_headers.txt

REM Verificar headers específicos
set "all_headers_present=true"

findstr /C:"X-Content-Type-Options: nosniff" temp_headers.txt >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ X-Content-Type-Options: nosniff%NC%
) else (
    echo %RED%❌ X-Content-Type-Options: nosniff (faltante)%NC%
    set "all_headers_present=false"
)

findstr /C:"X-Frame-Options: DENY" temp_headers.txt >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ X-Frame-Options: DENY%NC%
) else (
    echo %RED%❌ X-Frame-Options: DENY (faltante)%NC%
    set "all_headers_present=false"
)

findstr /C:"X-XSS-Protection: 1; mode=block" temp_headers.txt >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ X-XSS-Protection: 1; mode=block%NC%
) else (
    echo %RED%❌ X-XSS-Protection: 1; mode=block (faltante)%NC%
    set "all_headers_present=false"
)

REM Verificar que headers sensibles NO estén presentes
findstr /C:"Server:" temp_headers.txt >nul
if %errorlevel% equ 0 (
    echo %RED%❌ Server: (presente - información expuesta)%NC%
    set "all_headers_present=false"
) else (
    echo %GREEN%✅ Server: (no expuesto)%NC%
)

findstr /C:"X-Powered-By:" temp_headers.txt >nul
if %errorlevel% equ 0 (
    echo %RED%❌ X-Powered-By: (presente - información expuesta)%NC%
    set "all_headers_present=false"
) else (
    echo %GREEN%✅ X-Powered-By: (no expuesto)%NC%
)

if "!all_headers_present!"=="true" (
    echo %GREEN%✅ Todos los headers de seguridad están configurados correctamente%NC%
) else (
    echo %RED%❌ Algunos headers de seguridad están faltando o mal configurados%NC%
)
echo.

REM Función para verificar endpoints sensibles bloqueados
echo %BLUE%3. Verificando endpoints sensibles bloqueados...%NC%

set "all_blocked=true"

REM Verificar /actuator/env
curl -s -w "%%{http_code}" "%BASE_URL%/actuator/env" -o nul | findstr /R "403\|404" >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ /actuator/env bloqueado%NC%
) else (
    echo %RED%❌ /actuator/env accesible%NC%
    set "all_blocked=false"
)

REM Verificar /actuator/configprops
curl -s -w "%%{http_code}" "%BASE_URL%/actuator/configprops" -o nul | findstr /R "403\|404" >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ /actuator/configprops bloqueado%NC%
) else (
    echo %RED%❌ /actuator/configprops accesible%NC%
    set "all_blocked=false"
)

REM Verificar /actuator/beans
curl -s -w "%%{http_code}" "%BASE_URL%/actuator/beans" -o nul | findstr /R "403\|404" >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ /actuator/beans bloqueado%NC%
) else (
    echo %RED%❌ /actuator/beans accesible%NC%
    set "all_blocked=false"
)

if "!all_blocked!"=="true" (
    echo %GREEN%✅ Todos los endpoints sensibles están bloqueados%NC%
) else (
    echo %RED%❌ Algunos endpoints sensibles están accesibles%NC%
)
echo.

REM Función para verificar rate limiting
echo %BLUE%4. Verificando rate limiting...%NC%

echo    Enviando 101 requests rápidamente...

set "rate_limited=false"
for /l %%i in (1,1,101) do (
    curl -s -w "%%{http_code}" "%BASE_URL%%API_ENDPOINT%" -o nul | findstr "429" >nul
    if !errorlevel! equ 0 (
        set "rate_limited=true"
        echo %GREEN%✅ Rate limiting activado en request #%%i (HTTP 429)%NC%
        goto :rate_limit_done
    )
    
    REM Mostrar progreso cada 20 requests
    set /a "mod=%%i %% 20"
    if !mod! equ 0 (
        echo    Progreso: %%i/101 requests enviados...
    )
)

:rate_limit_done
if "!rate_limited!"=="true" (
    echo %GREEN%✅ Rate limiting está funcionando correctamente%NC%
) else (
    echo %YELLOW%⚠️  Rate limiting no se activó después de 101 requests%NC%
    echo    Esto podría indicar que el rate limiting no está configurado o es muy permisivo
)
echo.

REM Función para verificar CORS
echo %BLUE%5. Verificando configuración CORS...%NC%

REM Test con origen permitido
curl -s -H "Origin: http://localhost:3000" -H "Access-Control-Request-Method: GET" -H "Access-Control-Request-Headers: X-Requested-With" -X OPTIONS "%BASE_URL%%API_ENDPOINT%" | findstr "Access-Control-Allow-Origin" >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ CORS configurado para origen localhost:3000%NC%
) else (
    echo %RED%❌ CORS no configurado correctamente%NC%
)

REM Test con origen no permitido
curl -s -H "Origin: https://malicious-site.com" -H "Access-Control-Request-Method: GET" -X OPTIONS "%BASE_URL%%API_ENDPOINT%" | findstr "Access-Control-Allow-Origin" >nul
if %errorlevel% equ 0 (
    echo %RED%❌ CORS permite origen malicioso (mal configurado)%NC%
) else (
    echo %GREEN%✅ CORS bloquea origen malicioso correctamente%NC%
)
echo.

REM Función para verificar configuración de Swagger
echo %BLUE%6. Verificando configuración de Swagger...%NC%

curl -s -w "%%{http_code}" "%BASE_URL%/swagger-ui.html" -o nul | findstr "200" >nul
if %errorlevel% equ 0 (
    echo %YELLOW%⚠️  Swagger UI accesible (solo debe estar habilitado en desarrollo)%NC%
    echo    En producción, esto debe estar deshabilitado
) else (
    echo %GREEN%✅ Swagger UI no accesible%NC%
)

curl -s -w "%%{http_code}" "%BASE_URL%/v3/api-docs" -o nul | findstr "200" >nul
if %errorlevel% equ 0 (
    echo %YELLOW%⚠️  API Docs accesible (solo debe estar habilitado en desarrollo)%NC%
) else (
    echo %GREEN%✅ API Docs no accesible%NC%
)
echo.

REM Función para verificar logging
echo %BLUE%7. Verificando configuración de logging...%NC%

if exist "logs" (
    echo %GREEN%✅ Directorio de logs creado%NC%
) else (
    echo %YELLOW%⚠️  Directorio de logs no encontrado%NC%
    echo    Se creará automáticamente cuando la aplicación genere logs
)
echo.

REM Función para resumen final
echo %BLUE%📊 Resumen de Verificación de Seguridad%NC%
echo ==============================================
echo.
echo Este script ha verificado:
echo ✅ Headers de seguridad HTTP
echo ✅ Bloqueo de endpoints sensibles
echo ✅ Rate limiting
echo ✅ Configuración CORS
echo ✅ Configuración de Swagger
echo ✅ Configuración de logging
echo.
echo %GREEN%🎉 Verificación de seguridad completada!%NC%
echo.
echo Para más información, consulta:
echo 📖 SECURITY_README.md - Documentación completa de seguridad
echo 🔧 application-dev.yml - Configuración de desarrollo
echo 🚀 application-prod.yml - Configuración de producción

REM Limpiar archivos temporales
if exist "temp_headers.txt" del "temp_headers.txt"

endlocal
