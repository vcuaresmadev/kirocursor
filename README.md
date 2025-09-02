<<<<<<< HEAD
# 🔒 Microservicio de Distribución de Agua - Universidad Valle Grande

## 🚨 **IMPORTANTE: Medidas de Seguridad Implementadas**

Este proyecto ha sido configurado con **medidas de seguridad robustas** para prevenir la vulnerabilidad **API8:2023 Security Misconfiguration**.

### 🛡️ **Características de Seguridad**
- ✅ **Spring Security** configurado con WebFlux
- ✅ **Headers de seguridad HTTP** (X-Frame-Options, X-XSS-Protection, etc.)
- ✅ **Rate Limiting** para prevenir ataques DDoS
- ✅ **Configuración CORS restrictiva**
- ✅ **Control de endpoints sensibles**
- ✅ **Configuración por entorno** (desarrollo vs producción)
- ✅ **Docker con mejores prácticas de seguridad**

### 📖 **Documentación de Seguridad**
- **SECURITY_README.md** - Guía completa de seguridad
- **scripts/security-check.sh** - Script de verificación (Linux/Mac)
- **scripts/security-check.bat** - Script de verificación (Windows)

### 🚀 **Ejecución Rápida**
```bash
# Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Verificar seguridad
./scripts/security-check.sh  # Linux/Mac
scripts/security-check.bat   # Windows
```

---

# Análisis y Diseño de Entidades para Microservicios

## Microservicios Principales:

- [MS-ORGANIZACIONES (Multi-JASS)](#1-ms-organizaciones-multi-jass)
- [MS-USUARIOS-AUTENTICACION (Unificado)](#2-ms-usuarios-autenticacion)
- [MS-INFRAESTRUCTURA (Cajas, Asignación de Cajas)](#3-ms-infraestructura)
- [MS-PAGOS-FACTURACION (Pagos, Facturas, Recibos)](#4-ms-pagos-facturacion)
- [MS-DISTRIBUCION-AGUA (Horarios, Programación, Incidencias de la Distribución)](#5-ms-distribucion-agua)
- [MS-INVENTARIO-COMPRAS (Productos, Compras)](#6-ms-inventario-compras)
- [MS-CALIDAD-AGUA (Cloro, Análisis, Registros)](#7-ms-calidad-agua)
- [MS-RECLAMOS-INCIDENCIAS (Reclamos Generales, Incidencias)](#8-ms-reclamos-incidencias)
- [MS-NOTIFICACIONES (SMS, WhatsApp, Email)](#9-ms-notificaciones)
- API-GATEWAY (Enrutamiento, Seguridad)

## 1. MS-ORGANIZACIONES (Multi-JASS)

### Tablas Maestras:

- organizations (entidad principal)
- zones (ubicaciones geográficas)
- streets (calles dentro de las zonas)

### Tablas Transaccionales:

(Este microservicio es principalmente de configuración, no tiene muchas transacciones)

### Relaciones:

- Una organización tiene múltiples zonas
- Una zona tiene múltiples calles

```json
Collection: organizations

{
  "organization_id": ObjectId("..."),
  "organization_code": "JASS001",
  "organization_name": "JASS Rinconada de Conta",
  "legal_representative": "Juan Pérez",
  "address": "Av. Principal 123",
  "phone": "987654321",
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z"),
  "updated_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
Collection: zones

{
  "zone_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "zone_code": "ZN0001",
  "zone_name": "RINCONADA DE CONTA",
  "description": "CENTRO POBLADO RINCONADA DE CONTA",
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:05:00Z")
}
```

```json
Collection: streets

{
  "street_id": ObjectId("..."),
  "zone_id": ObjectId("..."),
  "street_code": "CAL001",
  "street_name": "Calle Los Pinos",
  "street_type": "CALLE",
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:10:00Z")
}
```

## 2. MS-USUARIOS-AUTENTICACION

### Tablas Maestras:

- users (datos básicos de usuarios)

- auth_credentials (credenciales de acceso)

### Tablas Transaccionales:

(Principalmente de gestión de usuarios y autenticación)

### Relaciones:

- Un usuario tiene unas credenciales de autenticación
- Un usuario pertenece a una organización

```json
Collection: users

{
  "user_id": ObjectId("..."),
  "user_code": "USR1001",
  "document_type": "DNI",
  "document_number": "87654321",
  "first_name": "María",
  "last_name": "Gonzales",
  "phone": "987654321",
  "email": "maria@gmail.com",
  "street_address": "Calle Los Pinos 123",
  "street_id": ObjectId("..."),
  "user_type": "CLIENT",
  "status": "ACTIVE",
  "registration_date": ISODate("2023-01-15"),
  "update_at": ISODate("2023-01-15T09:00:00Z")
  "water_boxes": [
    {
      "water_box_code": "CAJA001",
      "water_box_name": "CAJA 1",
      "status": "ACTIVE"
    },
    {
      "water_box_code": "CAJA002",
      "water_box_name": "CAJA 2",
      "status": "ACTIVE"
    }
  ],
}
```

```json
Collection: auth_credentials

{
  "auth_credential_id": ObjectId("..."),
  "user_id": ObjectId("..."),
  "username": "maria.gonzales",
  "password_hash": "hashed_password",
  "roles": ["CLIENT"],
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-15T09:10:00Z")
}
```

## 3. MS-INFRAESTRUCTURA

### Tablas Maestras:

- water_boxes (cajas de agua)


### Tablas Transaccionales:

- water_box_assignments (asignaciones a usuarios)

- water_box_transfers (transferencias entre usuarios)

### Relaciones:

- Una caja de agua puede tener múltiples asignaciones (histórico)

- Una transferencia relaciona dos asignaciones

```json
Collection: water_boxes
{
  "_id": ObjectId("665f1a2b3c4d5e6f78901234"),
  "organization_id": ObjectId("..."),
  "box_code": "CAJA-001",
  "box_type": "CAÑO", // CAÑO o BOMBA
  "installation_date": ISODate("2022-05-10"),
  "current_assignment_id": ObjectId("667a1b2c3d4e5f6789012345"),
  "status": "ACTIVE",
  "created_at": ISODate("2022-05-10T08:00:00Z")
}
```

```json
Collection: water_box_assignments (Transaccional - Cabecera)
{
  "_id": ObjectId("667a1b2c3d4e5f6789012345"), // Asignación actual
  "water_box_id": ObjectId("665f1a2b3c4d5e6f78901234"),
  "user_id": ObjectId("..."),
  "start_date": ISODate("2024-06-01"),
  "monthly_fee": 25.50,
  "status": "ACTIVE",
  "created_at": ISODate("2022-05-10T08:00:00Z"),
}
```

```json
Collection: water_box_transfers (Transaccional - Cabecera)
{
  "_id": ObjectId("668a1b2c3d4e5f6789012345"),
  "water_box_id": ObjectId("665f1a2b3c4d5e6f78901234"),
  "old_assignment_id": ObjectId("667b2c3d4e5f678901234567"), // asignación anterior
  "new_assignment_id": ObjectId("667a1b2c3d4e5f6789012345"), // nueva asignación
  "transfer_reason": "VENTA DE PROPIEDAD",
  "documents": [ // Opcional: documentos
    "/docs/transfers/668a..._dni.jpg"
  ],
  "created_at": ISODate("2024-06-01T14:30:00Z")
}
```

```json
Collection: assignment_history (Historial - Detalle)
// Misma colección water_box_assignments (filtrado por status=INACTIVE)
{
  "_id": ObjectId("667b2c3d4e5f678901234567"), // Asignación pasada
  "water_box_id": ObjectId("665f1a2b3c4d5e6f78901234"),
  "user_id": ObjectId("..."), // Dueño anterior
  "start_date": ISODate("2023-01-20"),
  "end_date": ISODate("2024-05-31"), // Fecha de fin obligatoria
  "status": "INACTIVE",
  "transfer_id": ObjectId("668a1b2c3d4e5f6789012345") // Ref. a transferencia
}
```

## 4. MS-PAGOS-FACTURACION

### Tablas Maestras:

Ninguna

### Tablas Transaccionales:

- payments (pagos realizados)

- payments_details (meses cubiertos por pagos)

- receipts (recibos emitidos)

### Relaciones:

- Un pago puede cubrir múltiples meses

- Un pago puede tener múltiples cargos adicionales

- Cada pago genera un recibo

```json
Collection: payments (Transaccional - Cabecera)
{
  "payment_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "payment_code": "PAG_0001",
  "user_id": ObjectId("..."),
  "water_box_id": ObjectId("..."),
  "payment_type": "SERVICIO_AGUA", // Puede ser: SERVICIO_AGUA, REPOSICION_CAJA, MIXTO
  "payment_method": "YAPE",
  "total_amount": 55.50, // 25.50 (servicio) + 30.00 (reposición)
  "payment_date": ISODate("2023-06-05"),
  "payment_status": "PAID",
  "external_reference": "YAPE123456",
  "created_at": ISODate("2023-06-05T11:20:00Z")
}
```

```json
Collection: payments_details (Transaccional - Detalle)
// Detalle 1: Servicio mensual
{
  "payment_details_id": ObjectId("..."),
  "payment_id": ObjectId("..."),
  "concept": "SERVICIO_AGUA",
  "year": 2023,
  "month": 6,
  "amount": 25.50,
  "description": "Servicio de agua mensual",
  "period_start": ISODate("2023-06-01"),
  "period_end": ISODate("2023-06-30")
}

// Detalle 2: Cargo adicional (reposición)
{
  "payment_details_id": ObjectId("..."),
  "payment_id": ObjectId("..."),
  "concept": "REPOSICION_CAJA",
  "amount": 30.00,
  "description": "Reposición de caja de agua cortada"
}
```

```json
Collection: receipts (Cabecera)
// Recibo 1: Servicio mensual
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "payment_id": ObjectId("..."),
  "payment_details_id": ObjectId("..."), // Referencia al detalle específico
  "receipt_series": "B001",
  "receipt_number": "000123",
  "receipt_type": "SERVICIO_MENSUAL",
  "issue_date": ISODate("2023-06-05"),
  "amount": 25.50,
  "year": 2023,
  "month": 6,
  "concept": "Servicio de Agua - Junio 2023",
  "customer_full_name": "María Gonzales",
  "customer_document": "DNI 87654321",
  "pdf_generated": true,
  "pdf_path": "/receipts/B001-000123.pdf"
}

// Recibo 2: Reposición
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "payment_id": ObjectId("..."),
  "payment_details_id": ObjectId("..."), // Referencia al detalle específico
  "receipt_series": "B001",
  "receipt_number": "000124",
  "receipt_type": "REPOSICION_CAJA",
  "issue_date": ISODate("2023-06-05"),
  "amount": 30.00,
  "concept": "Reposición de caja de agua",
  "customer_full_name": "María Gonzales",
  "customer_document": "DNI 87654321",
  "pdf_generated": true,
  "pdf_path": "/receipts/B001-000124.pdf"
}
```

## 5. MS-DISTRIBUCION-AGUA

### Tablas Maestras:

- distribution_schedules (horarios de distribución por zonas)
- distribution_routes (rutas de distribución)
- fares (tarifas de distribución)

### Tablas Transaccionales:

- distribution_programs (programación semanal/mensual)
- distribution_incidents (incidencias durante la distribución)

### Relaciones:

- Un horario puede tener múltiples programaciones
- Una programación puede tener múltiples incidencias
- Una ruta puede tener múltiples logs de distribución

```json
// Coleccion: fares (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "fare_code": "TAR001",
  "fare_name": "Tarifa Diaria",
  "fare_type": "DIARIA", // DIARIA, SEMANAL, MENSUAL
  "fare_amount": 25.50,
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: distribution_schedules (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "schedule_code": "HOR001",
  "zone_id": ObjectId("..."),
  "schedule_name": "Horario Zona Centro",
  "days_of_week": ["LUNES", "MIÉRCOLES", "VIERNES"],
  "start_time": "06:00",
  "end_time": "12:00",
  "duration_hours": 6,
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```
```json
// Colección: distribution_routes (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "route_code": "RUT001",
  "route_name": "Ruta Principal",
  "zones": [
    {
      "zone_id": ObjectId("..."),
      "order": 1,
      "estimated_duration": 2 // horas
    }
  ],
  "total_estimated_duration": 6,
  "responsible_user_id": ObjectId("..."),
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: distribution_programs (Transaccional - Cabecera)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "program_code": "PROG001",
  "schedule_id": ObjectId("..."),
  "route_id": ObjectId("..."),
  "program_date": ISODate("2023-06-05"),
  "planned_start_time": "06:00",
  "planned_end_time": "12:00",
  "actual_start_time": "06:15",
  "actual_end_time": "12:30",
  "status": "COMPLETED", // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
  "responsible_user_id": ObjectId("..."),
  "observations": "Distribución normal con retraso de 15 min al inicio",
  "created_at": ISODate("2023-06-04T18:00:00Z")
}
```

```json
// Colección: distribution_incidents (Transaccional - Detalle)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "program_id": ObjectId("..."),
  "incident_code": "INC001",
  "incident_type": "CORTE_SUMINISTRO", // CORTE_SUMINISTRO, BAJA_PRESION, FUGA_TUBERIA
  "zone_id": ObjectId("..."),
  "incident_time": ISODate("2023-06-05T08:30:00Z"),
  "resolution_time": ISODate("2023-06-05T10:15:00Z"),
  "description": "Rotura de tubería principal en Calle Los Pinos",
  "severity": "HIGH", // LOW, MEDIUM, HIGH, CRITICAL
  "affected_boxes": 25,
  "resolved": true,
  "resolution_notes": "Reparación temporal con válvula de bypass",
  "reported_by_user_id": ObjectId("..."),
  "resolved_by_user_id": ObjectId("...")
}
```

### 6. MS-INVENTARIO-COMPRAS
### Tablas Maestras:

- products (catálogo de productos)
- suppliers (proveedores)
- product_categories (categorías de productos)

### Tablas Transaccionales:

- purchases (órdenes de compra)
- purchase_details (detalle de productos comprados)
- inventory_movements (movimientos de inventario)
- stock_adjustments (ajustes de inventario)

### Relaciones:

- Un producto pertenece a una categoría
- Una compra tiene múltiples detalles
- Cada movimiento afecta el stock de un producto

```json
// Colección: product_categories (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "category_code": "CAT001",
  "category_name": "TUBERÍAS Y CONEXIONES",
  "description": "Materiales para instalación de tuberías",
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: products (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "product_code": "PROD001",
  "product_name": "Tubería PVC 1/2 pulgada",
  "category_id": ObjectId("..."),
  "unit_of_measure": "METRO",
  "minimum_stock": 50,
  "maximum_stock": 200,
  "current_stock": 125,
  "unit_cost": 3.50,
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: suppliers (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "supplier_code": "PROV001",
  "supplier_name": "Materiales Hidráulicos SAC",
  "contact_person": "Carlos Mendoza",
  "phone": "987654321",
  "email": "ventas@hidraulicos.com",
  "address": "Av. Industrial 456",
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: purchases (Transaccional - Cabecera)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "purchase_code": "COMP001",
  "supplier_id": ObjectId("..."),
  "purchase_date": ISODate("2023-06-05"),
  "delivery_date": ISODate("2023-06-08"),
  "total_amount": 1250.00,
  "status": "DELIVERED", // PENDING, ORDERED, DELIVERED, CANCELLED
  "requested_by_user_id": ObjectId("..."),
  "approved_by_user_id": ObjectId("..."),
  "invoice_number": "FAC-001234",
  "observations": "Entrega completa según especificaciones",
  "created_at": ISODate("2023-06-05T09:00:00Z")
}
```

```json
// Colección: purchase_details (Transaccional - Detalle)
{
  "_id": ObjectId("..."),
  "purchase_id": ObjectId("..."),
  "product_id": ObjectId("..."),
  "quantity_ordered": 100,
  "quantity_received": 100,
  "unit_cost": 3.50,
  "subtotal": 350.00,
  "observations": "Material recibido en buenas condiciones"
} 
```

```json
// Colección: inventory_movements (Transaccional - Detalle)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "product_id": ObjectId("..."),
  "movement_type": "ENTRADA", // ENTRADA, SALIDA, AJUSTE
  "movement_reason": "COMPRA", // COMPRA, VENTA, USO_INTERNO, AJUSTE, MERMA
  "quantity": 100,
  "unit_cost": 3.50,
  "reference_document": "COMP001",
  "reference_id": ObjectId("..."), // ID de compra, venta, etc.
  "previous_stock": 25,
  "new_stock": 125,
  "movement_date": ISODate("2023-06-08T14:00:00Z"),
  "user_id": ObjectId("..."),
  "observations": "Ingreso por compra COMP001"
}
```

## 7. MS-CALIDAD-AGUA

### Tablas Maestras:

- quality_parameters (parámetros de calidad)
- testing_points (puntos de muestreo)

### Tablas Transaccionales:

- quality_tests (análisis de calidad)
- chlorine_records (registros de cloro)
- quality_incidents (incidencias de calidad)

### Relaciones:

- Un análisis incluye múltiples parámetros
- Un punto de muestreo puede tener múltiples registros
- Una incidencia puede requerir múltiples análisis

```json
// Colección: quality_parameters (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "parameter_code": "CLORO_LIBRE",
  "parameter_name": "Cloro Libre Residual",
  "unit_of_measure": "mg/L",
  "min_acceptable": 0.3,
  "max_acceptable": 1.5,
  "optimal_range": {
    "min": 0.5,
    "max": 1.0
  },
  "test_frequency": "DAILY", // DAILY, WEEKLY, MONTHLY
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: testing_points (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "point_code": "PM001",
  "point_name": "Reservorio Principal",
  "point_type": "RESERVORIO", // RESERVORIO, RED_DISTRIBUCION, DOMICILIO
  "zone_id": ObjectId("..."),
  "location_description": "Entrada del reservorio principal",
  "coordinates": {
    "latitude": -12.0464,
    "longitude": -77.0428
  },
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: quality_tests (Transaccional - Cabecera)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "test_code": "ANL001",
  "testing_point_id": ObjectId("..."),
  "test_date": ISODate("2023-06-05T08:00:00Z"),
  "test_type": "RUTINARIO", // RUTINARIO, ESPECIAL, INCIDENCIA
  "tested_by_user_id": ObjectId("..."),
  "weather_conditions": "SOLEADO",
  "water_temperature": 18.5,
  "general_observations": "Agua clara, sin olor ni sabor extraño",
  "status": "COMPLETED",
  "results": [
    {
      "parameter_id": ObjectId("..."),
      "parameter_code": "CLORO_LIBRE",
      "measured_value": 0.8,
      "unit": "mg/L",
      "status": "ACCEPTABLE", // ACCEPTABLE, WARNING, CRITICAL
      "observations": "Dentro del rango óptimo"
    },
    {
      "parameter_id": ObjectId("..."),
      "parameter_code": "PH",
      "measured_value": 7.2,
      "unit": "pH",
      "status": "ACCEPTABLE",
      "observations": "pH neutro adecuado"
    }
  ],
  "created_at": ISODate("2023-06-05T08:30:00Z")
}
```

```json
// Colección: chlorine_records (Transaccional - Específica)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "record_code": "CL001",
  "testing_point_id": ObjectId("..."),
  "record_date": ISODate("2023-06-05T08:00:00Z"),
  "chlorine_level": 0.8,
  "acceptable": true,
  "action_required": false,
  "recorded_by_user_id": ObjectId("..."),
  "observations": "Nivel óptimo de cloro",
  "next_chlorination_date": ISODate("2023-06-12T08:00:00Z"),
  "created_at": ISODate("2023-06-05T08:15:00Z")
}
```

```json
// Colección: quality_incidents (Transaccional)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "incident_code": "INC_CAL001",
  "incident_type": "CLORO_BAJO", // CLORO_BAJO, TURBIDEZ_ALTA, CONTAMINACION
  "testing_point_id": ObjectId("..."),
  "detection_date": ISODate("2023-06-05T10:00:00Z"),
  "severity": "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
  "description": "Nivel de cloro por debajo del mínimo aceptable",
  "affected_zones": [ObjectId("...")],
  "immediate_actions": "Incremento de dosificación de cloro",
  "corrective_actions": "Verificación del sistema de cloración",
  "resolved": true,
  "resolution_date": ISODate("2023-06-05T14:00:00Z"),
  "reported_by_user_id": ObjectId("..."),
  "resolved_by_user_id": ObjectId("...")
}
```

## 8. MS-RECLAMOS-INCIDENCIAS
### Tablas Maestras:

- complaint_categories (categorías de reclamos)
- incident_types (tipos de incidencias)

### Tablas Transaccionales:

- complaints (reclamos de usuarios)
- incidents (incidencias generales)
- complaint_responses (respuestas a reclamos)
- incident_resolutions (resoluciones de incidencias)

### Relaciones:

- Un reclamo puede tener múltiples respuestas
- Una incidencia puede tener una resolución
- Un reclamo pertenece a una categoría

```json
// Colección: complaint_categories (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "category_code": "CAT_REC001",
  "category_name": "FALTA DE AGUA",
  "description": "Reclamos relacionados con falta de suministro de agua",
  "priority_level": "HIGH", // LOW, MEDIUM, HIGH, CRITICAL
  "max_response_time": 24, // horas
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: incident_types (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "type_code": "INC_TYPE001",
  "type_name": "FUGA DE TUBERÍA",
  "description": "Fugas en la red de distribución",
  "priority_level": "HIGH",
  "estimated_resolution_time": 4, // horas
  "requires_external_service": false,
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: complaints (Transaccional - Cabecera)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "complaint_code": "REC001",
  "user_id": ObjectId("..."),
  "category_id": ObjectId("..."),
  "water_box_id": ObjectId("..."),
  "complaint_date": ISODate("2023-06-05T14:30:00Z"),
  "subject": "Falta de agua hace 3 días",
  "description": "No llega agua a mi domicilio desde hace 3 días, necesito solución urgente",
  "priority": "HIGH",
  "status": "IN_PROGRESS", // RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
  "assigned_to_user_id": ObjectId("..."),
  "expected_resolution_date": ISODate("2023-06-06T14:30:00Z"),
  "actual_resolution_date": null,
  "satisfaction_rating": null, // 1-5 cuando se resuelve
  "created_at": ISODate("2023-06-05T14:30:00Z")
}
```

```json
// Colección: complaint_responses (Transaccional - Detalle)
{
  "_id": ObjectId("..."),
  "complaint_id": ObjectId("..."),
  "response_date": ISODate("2023-06-05T16:00:00Z"),
  "response_type": "INVESTIGACION", // INVESTIGACION, SOLUCION, SEGUIMIENTO
  "message": "Hemos verificado la zona y encontramos una fuga en la tubería principal. Procederemos con la reparación mañana temprano.",
  "responded_by_user_id": ObjectId("..."),
  "internal_notes": "Fuga confirmada en Calle Los Pinos, requiere excavación",
  "created_at": ISODate("2023-06-05T16:00:00Z")
}
```

```json
// Colección: incidents (Transaccional - Cabecera)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "incident_code": "INC001",
  "incident_type_id": ObjectId("..."),
  "zone_id": ObjectId("..."),
  "incident_date": ISODate("2023-06-05T12:00:00Z"),
  "title": "Fuga en Calle Los Pinos",
  "description": "Fuga importante en tubería principal, afecta suministro a 30 viviendas",
  "severity": "HIGH",
  "status": "RESOLVED", // REPORTED, IN_PROGRESS, RESOLVED, CLOSED
  "affected_boxes_count": 30,
  "reported_by_user_id": ObjectId("..."),
  "assigned_to_user_id": ObjectId("..."),
  "estimated_resolution": ISODate("2023-06-06T08:00:00Z"),
  "actual_resolution": ISODate("2023-06-06T10:30:00Z"),
  "related_complaints": [ObjectId("...")], // Referencias a reclamos relacionados
  "created_at": ISODate("2023-06-05T12:00:00Z")
}
```

```json
// Colección: incident_resolutions (Transaccional - Detalle)
{
  "_id": ObjectId("..."),
  "incident_id": ObjectId("..."),
  "resolution_date": ISODate("2023-06-06T10:30:00Z"),
  "resolution_type": "REPARACION_COMPLETA", // REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
  "actions_taken": "Excavación y reemplazo de 5 metros de tubería principal",
  "materials_used": [
    {
      "product_id": ObjectId("..."),
      "quantity": 5,
      "unit": "METRO"
    }
  ],
  "labor_hours": 6,
  "total_cost": 450.00,
  "resolved_by_user_id": ObjectId("..."),
  "quality_check": true,
  "follow_up_required": false,
  "resolution_notes": "Reparación completada exitosamente, sistema funcionando normalmente",
  "created_at": ISODate("2023-06-06T10:30:00Z")
}
```

## 9. MS-NOTIFICACIONES
### Tablas Maestras:

- notification_templates (plantillas de mensajes)
- notification_channels (canales de comunicación)

### Tablas Transaccionales:

- notifications (notificaciones enviadas)
- notification_logs (logs de envío)

### Relaciones:

- Una notificación usa una plantilla
- Una notificación puede enviarse por múltiples canales
- Cada envío genera un log

```json
// Colección: notification_templates (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "template_code": "PAYMENT_REMINDER",
  "template_name": "Recordatorio de Pago",
  "template_type": "PAYMENT", // PAYMENT, INCIDENT, MAINTENANCE, GENERAL
  "channels": ["SMS", "WHATSAPP", "EMAIL"],
  "templates": {
    "sms": {
      "subject": null,
      "body": "Estimado {user_name}, le recordamos que tiene un pago pendiente de S/ {amount} correspondiente al mes de {month}. Gracias."
    },
    "whatsapp": {
      "subject": "Recordatorio de Pago - JASS",
      "body": "Hola {user_name} 👋\n\nTe recordamos que tienes un pago pendiente:\n💰 Monto: S/ {amount}\n📅 Mes: {month}\n\nPuedes realizar tu pago contactándonos. ¡Gracias!"
    },
    "email": {
      "subject": "Recordatorio de Pago - {organization_name}",
      "body": "<h2>Recordatorio de Pago</h2><p>Estimado/a {user_name},</p><p>Le recordamos que tiene un pago pendiente de <strong>S/ {amount}</strong> correspondiente al mes de <strong>{month}</strong>.</p><p>Gracias por su atención.</p>"
    }
  },
  "variables": ["user_name", "amount", "month", "organization_name"],
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```

```json
// Colección: notification_channels (Maestra)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "channel_code": "WHATSAPP",
  "channel_name": "WhatsApp Business",
  "channel_type": "WHATSAPP",
  "configuration": {
    "api_url": "https://api.whatsapp.com/send",
    "api_key": "encrypted_key",
    "phone_number": "+51987654321",
    "business_account_id": "123456789"
  },
  "daily_limit": 1000,
  "cost_per_message": 0.05,
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}
```
#01
```json
// Colección: notifications (Transaccional - Cabecera)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "notification_code": "NOT001",
  "template_id": ObjectId("..."),
  "notification_type": "PAYMENT_REMINDER",
  "recipient_user_id": ObjectId("..."),
  "recipient_info": {
    "name": "María Gonzales",
    "phone": "+51987654321",
    "email": "maria@gmail.com"
  },
  "variables_data": {
    "user_name": "María Gonzales",
    "amount": "25.50",
    "month": "Junio 2023",
    "organization_name": "JASS Rinconada de Conta"
  },
  "scheduled_date": ISODate("2023-06-10T09:00:00Z"),
  "priority": "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
  "status": "SENT", // PENDING, SENT, FAILED, CANCELLED
  "channels_to_send": ["SMS", "WHATSAPP"],
  "total_cost": 0.10,
  "created_at": ISODate("2023-06-09T18:00:00Z")
}
```
#02
```json
// Colección: notification_logs (Transaccional - Detalle)
{
  "_id": ObjectId("..."),
  "notification_id": ObjectId("..."),
  "channel_id": ObjectId("..."),
  "channel_type": "WHATSAPP",
  "recipient": "+51987654321",
  "sent_date": ISODate("2023-06-10T09:00:00Z"),
  "status": "DELIVERED", // SENT, DELIVERED, FAILED, READ
  "external_message_id": "wamid.xyz123",
  "delivery_status": "delivered",
  "read_date": ISODate("2023-06-10T09:15:00Z"),
  "cost": 0.05,
  "error_message": null,
  "retry_count": 0,
  "final_content": {
    "subject": "Recordatorio de Pago - JASS",
    "body": "Hola María Gonzales 👋\n\nTe recordamos que tienes un pago pendiente:\n💰 Monto: S/ 25.50\n📅 Mes: Junio 2023\n\nPuedes realizar tu pago contactándonos. ¡Gracias!"
  },
  "created_at": ISODate("2023-06-10T09:00:00Z")
}
=======
# TestDistribution
>>>>>>> 489117b1379545859c38d0d1c6e133b4e9283239
=======
## Hi there 👋

<!--
**kirocursor/kirocursor** is a ✨ _special_ ✨ repository because its `README.md` (this file) appears on your GitHub profile.

Here are some ideas to get you started:

- 🔭 I’m currently working on ...
- 🌱 I’m currently learning ...
- 👯 I’m looking to collaborate on ...
- 🤔 I’m looking for help with ...
- 💬 Ask me about ...
- 📫 How to reach me: ...
- 😄 Pronouns: ...
- ⚡ Fun fact: ...
-->
>>>>>>> 78be20a84b5b407c57fe243eb52ab5b80d6dd32b
