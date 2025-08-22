# Arquitectura de Base de Datos Híbrida - Sistema JASS

## Descripción
Este documento detalla la estructura de la base de datos híbrida utilizada en el Sistema JASS. El sistema implementa una arquitectura que combina bases de datos relacionales (PostgreSQL) para transacciones y datos estructurados, y bases de datos NoSQL (MongoDB) para datos más flexibles y escalables.

## Índice
1. [Bases de Datos Relacionales (PostgreSQL)](#bases-de-datos-relacionales-postgresql)
   - [MS-PAGOS-FACTURACION](#1-ms-pagos-facturacion)
   - [MS-INFRAESTRUCTURA](#2-ms-infraestructura)
   - [MS-INVENTARIO-COMPRAS](#3-ms-inventario-compras)

2. [Bases de Datos NoSQL (MongoDB)](#bases-de-datos-nosql-mongodb)
   - [MS-ORGANIZACIONES](#1-ms-organizaciones)
   - [MS-USUARIOS-AUTENTICACION](#2-ms-usuarios-autenticacion)
   - [MS-DISTRIBUCION-AGUA](#3-ms-distribucion-agua)
   - [MS-CALIDAD-AGUA](#4-ms-calidad-agua)
   - [MS-RECLAMOS-INCIDENCIAS](#5-ms-reclamos-incidencias)
   - [MS-NOTIFICACIONES](#6-ms-notificaciones)

## Bases de Datos Relacionales (PostgreSQL)
### 1. MS-PAGOS-FACTURACION

```sql
-- Tabla principal de pagos
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    payment_code VARCHAR(20) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    water_box_id UUID NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- SERVICIO_AGUA, REPOSICION_CAJA, MIXTO
    payment_method VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    external_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Detalle de pagos
CREATE TABLE payment_details (
    payment_detail_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(payment_id),
    concept VARCHAR(50) NOT NULL,
    year INTEGER,
    month INTEGER,
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,
    period_start DATE,
    period_end DATE
);

-- Recibos
CREATE TABLE receipts (
    receipt_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    payment_id UUID NOT NULL REFERENCES payments(payment_id),
    payment_detail_id UUID NOT NULL REFERENCES payment_details(payment_detail_id),
    receipt_series VARCHAR(10) NOT NULL,
    receipt_number VARCHAR(10) NOT NULL,
    receipt_type VARCHAR(30) NOT NULL,
    issue_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    year INTEGER,
    month INTEGER,
    concept TEXT,
    customer_full_name VARCHAR(200),
    customer_document VARCHAR(20),
    pdf_generated BOOLEAN DEFAULT FALSE,
    pdf_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimización
CREATE INDEX idx_payments_user_date ON payments(user_id, payment_date);
CREATE INDEX idx_payments_organization ON payments(organization_id);
CREATE INDEX idx_receipts_series_number ON receipts(receipt_series, receipt_number);
```

### 2. MS-INFRAESTRUCTURA

```sql
-- Cajas de agua
CREATE TABLE water_boxes (
    water_box_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    box_code VARCHAR(20) UNIQUE NOT NULL,
    box_type VARCHAR(10) NOT NULL, -- CAÑO, BOMBA
    installation_date DATE,
    current_assignment_id UUID,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Asignaciones de cajas
CREATE TABLE water_box_assignments (
    assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    water_box_id UUID NOT NULL REFERENCES water_boxes(water_box_id),
    user_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    monthly_fee DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    transfer_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transferencias
CREATE TABLE water_box_transfers (
    transfer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    water_box_id UUID NOT NULL REFERENCES water_boxes(water_box_id),
    old_assignment_id UUID NOT NULL REFERENCES water_box_assignments(assignment_id),
    new_assignment_id UUID NOT NULL REFERENCES water_box_assignments(assignment_id),
    transfer_reason TEXT,
    documents TEXT[], -- Array de rutas de documentos
    transfer_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Actualizar referencia en water_boxes
ALTER TABLE water_boxes 
ADD CONSTRAINT fk_current_assignment 
FOREIGN KEY (current_assignment_id) REFERENCES water_box_assignments(assignment_id);

-- Índices
CREATE INDEX idx_assignments_user ON water_box_assignments(user_id);
CREATE INDEX idx_assignments_box ON water_box_assignments(water_box_id);
CREATE INDEX idx_transfers_box ON water_box_transfers(water_box_id);
```

### 3. MS-INVENTARIO-COMPRAS
```sql
-- Categorías de productos
CREATE TABLE product_categories (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    category_code VARCHAR(20) UNIQUE NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Productos
CREATE TABLE products (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    product_code VARCHAR(20) UNIQUE NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    category_id UUID NOT NULL REFERENCES product_categories(category_id),
    unit_of_measure VARCHAR(20) NOT NULL,
    minimum_stock INTEGER DEFAULT 0,
    maximum_stock INTEGER,
    current_stock INTEGER DEFAULT 0,
    unit_cost DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Proveedores
CREATE TABLE suppliers (
    supplier_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    supplier_code VARCHAR(20) UNIQUE NOT NULL,
    supplier_name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Compras
CREATE TABLE purchases (
    purchase_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    purchase_code VARCHAR(20) UNIQUE NOT NULL,
    supplier_id UUID NOT NULL REFERENCES suppliers(supplier_id),
    purchase_date DATE NOT NULL,
    delivery_date DATE,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    requested_by_user_id UUID NOT NULL,
    approved_by_user_id UUID,
    invoice_number VARCHAR(50),
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Detalle de compras
CREATE TABLE purchase_details (
    purchase_detail_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_id UUID NOT NULL REFERENCES purchases(purchase_id),
    product_id UUID NOT NULL REFERENCES products(product_id),
    quantity_ordered INTEGER NOT NULL,
    quantity_received INTEGER DEFAULT 0,
    unit_cost DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    observations TEXT
);

-- Movimientos de inventario
CREATE TABLE inventory_movements (
    movement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    product_id UUID NOT NULL REFERENCES products(product_id),
    movement_type VARCHAR(20) NOT NULL, -- ENTRADA, SALIDA, AJUSTE
    movement_reason VARCHAR(30) NOT NULL, -- COMPRA, VENTA, USO_INTERNO, AJUSTE, MERMA
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(10,2),
    reference_document VARCHAR(50),
    reference_id UUID,
    previous_stock INTEGER NOT NULL,
    new_stock INTEGER NOT NULL,
    movement_date TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_products_organization ON products(organization_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_movements_product_date ON inventory_movements(product_id, movement_date);
CREATE INDEX idx_purchases_supplier ON purchases(supplier_id);
```
## Bases de Datos NoSQL (MongoDB)

### 1. MS-ORGANIZACIONES
```json
// Colección: organizations
{
  "_id": ObjectId("..."),
  "organization_code": "JASS001",
  "organization_name": "JASS Rinconada de Conta",
  "legal_representative": "Juan Pérez",
  "contact": {
    "address": "Av. Principal 123",
    "phone": "987654321",
    "email": "contacto@jass001.com"
  },
  "zones": [
    {
      "zone_id": ObjectId("..."),
      "zone_code": "ZN0001",
      "zone_name": "RINCONADA DE CONTA",
      "description": "CENTRO POBLADO RINCONADA DE CONTA",
      "coordinates": {
        "latitude": -12.0464,
        "longitude": -77.0428
      },
      "streets": [
        {
          "street_id": ObjectId("..."),
          "street_code": "CAL001",
          "street_name": "Calle Los Pinos",
          "street_type": "CALLE",
          "status": "ACTIVE"
        }
      ],
      "status": "ACTIVE"
    }
  ],
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z"),
  "updated_at": ISODate("2023-01-10T10:00:00Z")
}

// Índices
db.organizations.createIndex({ "organization_code": 1 })
db.organizations.createIndex({ "zones.zone_code": 1 })
db.organizations.createIndex({ "zones.streets.street_code": 1 })
```

### 2. MS-USUARIOS-AUTENTICACION
```json
// Colección: users
{
  "_id": ObjectId("..."),
  "user_code": "USR1001",
  "organization_id": ObjectId("..."),
  "personal_info": {
    "document_type": "DNI",
    "document_number": "87654321",
    "first_name": "María",
    "last_name": "Gonzales",
    "full_name": "María Gonzales"
  },
  "contact": {
    "phone": "987654321",
    "email": "maria@gmail.com",
    "address": {
      "street_address": "Calle Los Pinos 123",
      "street_id": ObjectId("..."),
      "zone_id": ObjectId("...")
    }
  },
  "user_type": "CLIENT", // CLIENT, ADMIN, OPERATOR, TECHNICIAN
  "water_boxes": [
    {
      "water_box_id": ObjectId("..."),
      "water_box_code": "CAJA001",
      "assignment_status": "ACTIVE",
      "monthly_fee": 25.50,
      "assignment_date": ISODate("2023-01-15")
    }
  ],
  "status": "ACTIVE",
  "registration_date": ISODate("2023-01-15"),
  "last_login": ISODate("2023-06-10T14:30:00Z"),
  "created_at": ISODate("2023-01-15T09:00:00Z"),
  "updated_at": ISODate("2023-06-10T14:30:00Z")
}

// Colección: auth_credentials
{
  "_id": ObjectId("..."),
  "user_id": ObjectId("..."),
  "username": "maria.gonzales",
  "password_hash": "$2b$10$...",
  "roles": ["CLIENT"],
  "permissions": [
    "view_own_payments",
    "view_own_bills",
    "submit_complaints"
  ],
  "last_password_change": ISODate("2023-01-15T09:10:00Z"),
  "failed_login_attempts": 0,
  "account_locked": false,
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-15T09:10:00Z")
}

// Índices
db.users.createIndex({ "user_code": 1 })
db.users.createIndex({ "personal_info.document_number": 1 })
db.users.createIndex({ "contact.email": 1 })
db.auth_credentials.createIndex({ "username": 1 }, { unique: true })
db.auth_credentials.createIndex({ "user_id": 1 })
```

### 3. MS-DISTRIBUCION-AGUA
```json
// Colección: distribution_schedules
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "schedule_code": "HOR001",
  "schedule_name": "Horario Zona Centro",
  "zones": [
    {
      "zone_id": ObjectId("..."),
      "zone_name": "RINCONADA DE CONTA",
      "priority": 1
    }
  ],
  "schedule_config": {
    "days_of_week": ["LUNES", "MIÉRCOLES", "VIERNES"],
    "start_time": "06:00",
    "end_time": "12:00",
    "duration_hours": 6,
    "water_flow_rate": 150 // litros por minuto
  },
  "routes": [
    {
      "route_id": ObjectId("..."),
      "route_name": "Ruta Principal",
      "sequence": 1,
      "estimated_duration": 2
    }
  ],
  "status": "ACTIVE",
  "created_at": ISODate("2023-01-10T10:00:00Z")
}

// Colección: distribution_programs
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "program_code": "PROG001",
  "schedule_id": ObjectId("..."),
  "program_date": ISODate("2023-06-05"),
  "execution": {
    "planned_start_time": "06:00",
    "planned_end_time": "12:00",
    "actual_start_time": "06:15",
    "actual_end_time": "12:30",
    "delay_minutes": 15
  },
  "responsible": {
    "user_id": ObjectId("..."),
    "user_name": "Carlos Técnico"
  },
  "zones_covered": [
    {
      "zone_id": ObjectId("..."),
      "start_time": "06:15",
      "end_time": "08:00",
      "water_distributed": 15000, // litros
      "boxes_served": 45
    }
  ],
  "incidents": [
    {
      "incident_id": ObjectId("..."),
      "incident_time": ISODate("2023-06-05T08:30:00Z"),
      "incident_type": "BAJA_PRESION",
      "description": "Presión baja en sector norte",
      "resolved": true,
      "resolution_time": ISODate("2023-06-05T09:00:00Z")
    }
  ],
  "status": "COMPLETED",
  "observations": "Distribución normal con retraso inicial",
  "created_at": ISODate("2023-06-04T18:00:00Z")
}

// Índices
db.distribution_schedules.createIndex({ "organization_id": 1, "schedule_code": 1 })
db.distribution_programs.createIndex({ "program_date": 1, "organization_id": 1 })
db.distribution_programs.createIndex({ "status": 1 })
```

### 4. MS-CALIDAD-AGUA
```json
// Colección: quality_tests
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "test_code": "ANL001",
  "testing_point": {
    "point_id": ObjectId("..."),
    "point_code": "PM001",
    "point_name": "Reservorio Principal",
    "point_type": "RESERVORIO",
    "location": {
      "zone_id": ObjectId("..."),
      "coordinates": {
        "latitude": -12.0464,
        "longitude": -77.0428
      },
      "description": "Entrada del reservorio principal"
    }
  },
  "test_info": {
    "test_date": ISODate("2023-06-05T08:00:00Z"),
    "test_type": "RUTINARIO",
    "tested_by": {
      "user_id": ObjectId("..."),
      "user_name": "Ana Química"
    },
    "environmental_conditions": {
      "weather": "SOLEADO",
      "temperature": 18.5,
      "humidity": 65
    }
  },
  "parameters": [
    {
      "parameter_code": "CLORO_LIBRE",
      "parameter_name": "Cloro Libre Residual",
      "measured_value": 0.8,
      "unit": "mg/L",
      "acceptable_range": {
        "min": 0.3,
        "max": 1.5,
        "optimal_min": 0.5,
        "optimal_max": 1.0
      },
      "status": "ACCEPTABLE",
      "observations": "Dentro del rango óptimo"
    },
    {
      "parameter_code": "PH",
      "parameter_name": "Potencial de Hidrógeno",
      "measured_value": 7.2,
      "unit": "pH",
      "acceptable_range": {
        "min": 6.5,
        "max": 8.5
      },
      "status": "ACCEPTABLE",
      "observations": "pH neutro adecuado"
    }
  ],
  "overall_status": "ACCEPTABLE",
  "general_observations": "Agua clara, sin olor ni sabor extraño",
  "follow_up_required": false,
  "next_test_date": ISODate("2023-06-12T08:00:00Z"),
  "attachments": [
    "/quality_tests/ANL001_photos.jpg",
    "/quality_tests/ANL001_lab_results.pdf"
  ],
  "created_at": ISODate("2023-06-05T08:30:00Z")
}

// Colección: chlorine_records (para registros diarios rápidos)
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "record_code": "CL001",
  "testing_point_id": ObjectId("..."),
  "record_date": ISODate("2023-06-05T08:00:00Z"),
  "chlorine_level": 0.8,
  "status": "ACCEPTABLE",
  "action_required": false,
  "recorded_by": {
    "user_id": ObjectId("..."),
    "user_name": "Ana Química"
  },
  "observations": "Nivel óptimo de cloro",
  "next_chlorination_date": ISODate("2023-06-12T08:00:00Z"),
  "created_at": ISODate("2023-06-05T08:15:00Z")
}

// Índices
db.quality_tests.createIndex({ "test_date": 1, "organization_id": 1 })
db.quality_tests.createIndex({ "testing_point.point_id": 1 })
db.chlorine_records.createIndex({ "record_date": 1, "testing_point_id": 1 })
```

### 5. MS-RECLAMOS-INCIDENCIAS
```json
// Colección: complaints
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "complaint_code": "REC001",
  "customer": {
    "user_id": ObjectId("..."),
    "user_name": "María Gonzales",
    "contact": {
      "phone": "987654321",
      "email": "maria@gmail.com"
    },
    "water_box": {
      "water_box_id": ObjectId("..."),
      "box_code": "CAJA001"
    }
  },
  "complaint_info": {
    "category": {
      "category_id": ObjectId("..."),
      "category_name": "FALTA DE AGUA",
      "priority_level": "HIGH"
    },
    "subject": "Falta de agua hace 3 días",
    "description": "No llega agua a mi domicilio desde hace 3 días, necesito solución urgente",
    "complaint_date": ISODate("2023-06-05T14:30:00Z"),
    "priority": "HIGH"
  },
  "assignment": {
    "assigned_to": {
      "user_id": ObjectId("..."),
      "user_name": "Carlos Técnico"
    },
    "assigned_date": ISODate("2023-06-05T15:00:00Z")
  },
  "timeline": {
    "expected_resolution_date": ISODate("2023-06-06T14:30:00Z"),
    "actual_resolution_date": ISODate("2023-06-06T10:30:00Z"),
    "response_time_hours": 19.5
  },
  "responses": [
    {
      "response_id": ObjectId("..."),
      "response_date": ISODate("2023-06-05T16:00:00Z"),
      "response_type": "INVESTIGACION",
      "message": "Hemos verificado la zona y encontramos una fuga en la tubería principal. Procederemos con la reparación mañana temprano.",
      "responded_by": {
        "user_id": ObjectId("..."),
        "user_name": "Carlos Técnico"
      },
      "internal_notes": "Fuga confirmada en Calle Los Pinos, requiere excavación",
      "attachments": ["/complaints/REC001_evidence.jpg"]
    }
  ],
  "resolution": {
    "resolution_date": ISODate("2023-06-06T10:30:00Z"),
    "resolution_type": "PROBLEMA_RESUELTO",
    "resolution_notes": "Fuga reparada, servicio restaurado completamente",
    "customer_satisfaction": {
      "rating": 5,
      "feedback": "Excelente atención y rápida solución"
    }
  },
  "status": "RESOLVED",
  "tags": ["fuga", "tuberia", "alta_prioridad"],
  "created_at": ISODate("2023-06-05T14:30:00Z"),
  "updated_at": ISODate("2023-06-06T10:30:00Z")
}

// Colección: incidents
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "incident_code": "INC001",
  "incident_info": {
    "title": "Fuga en Calle Los Pinos",
    "description": "Fuga importante en tubería principal, afecta suministro a 30 viviendas",
    "incident_type": {
      "type_id": ObjectId("..."),
      "type_name": "FUGA DE TUBERÍA",
      "priority_level": "HIGH"
    },
    "incident_date": ISODate("2023-06-05T12:00:00Z"),
    "severity": "HIGH"
  },
  "location": {
    "zone_id": ObjectId("..."),
    "zone_name": "RINCONADA DE CONTA",
    "specific_location": "Calle Los Pinos, altura cuadra 1",
    "coordinates": {
      "latitude": -12.0464,
      "longitude": -77.0428
    }
  },
  "impact": {
    "affected_boxes_count": 30,
    "affected_users": [
      {
        "user_id": ObjectId("..."),
        "user_name": "María Gonzales"
      }
    ],
    "estimated_repair_cost": 450.00
  },
  "assignment": {
    "reported_by": {
      "user_id": ObjectId("..."),
      "user_name": "María Gonzales"
    },
    "assigned_to": {
      "user_id": ObjectId("..."),
      "user_name": "Carlos Técnico"
    }
  },
  "timeline": {
    "estimated_resolution": ISODate("2023-06-06T08:00:00Z"),
    "actual_resolution": ISODate("2023-06-06T10:30:00Z")
  },
  "resolution": {
    "resolution_type": "REPARACION_COMPLETA",
    "actions_taken": "Excavación y reemplazo de 5 metros de tubería principal",
    "materials_used": [
      {
        "product_name": "Tubería PVC 1/2 pulgada",
        "quantity": 5,
        "unit": "METRO",
        "cost": 17.50
      }
    ],
    "labor_hours": 6,
    "total_cost": 450.00,
    "quality_check": true,
    "follow_up_required": false,
    "resolution_notes": "Reparación completada exitosamente, sistema funcionando normalmente"
  },
  "related_complaints": [ObjectId("...")],
  "status": "RESOLVED",
  "attachments": [
    "/incidents/INC001_before.jpg",
    "/incidents/INC001_after.jpg",
    "/incidents/INC001_invoice.pdf"
  ],
  "created_at": ISODate("2023-06-05T12:00:00Z"),
  "updated_at": ISODate("2023-06-06T10:30:00Z")
}

// Índices
db.complaints.createIndex({ "complaint_code": 1 })
db.complaints.createIndex({ "customer.user_id": 1 })
db.complaints.createIndex({ "status": 1, "complaint_info.complaint_date": 1 })
db.incidents.createIndex({ "incident_code": 1 })
db.incidents.createIndex({ "location.zone_id": 1 })
db.incidents.createIndex({ "status": 1, "incident_info.incident_date": 1 })
```

### 6. MS-NOTIFICACIONES
```json
// Colección: notification_templates
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "template_code": "PAYMENT_REMINDER",
  "template_name": "Recordatorio de Pago",
  "template_type": "PAYMENT",
  "channels": ["SMS", "WHATSAPP", "EMAIL"],
  "templates": {
    "sms": {
      "body": "Estimado {user_name}, le recordamos que tiene un pago pendiente de S/ {amount} correspondiente al mes de {month}. Gracias."
    },
    "whatsapp": {
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

// Colección: notifications
{
  "_id": ObjectId("..."),
  "organization_id": ObjectId("..."),
  "notification_code": "NOT001",
  "template": {
    "template_id": ObjectId("..."),
    "template_code": "PAYMENT_REMINDER"
  },
  "recipient": {
    "user_id": ObjectId("..."),
    "name": "María Gonzales",
    "contacts": {
      "phone": "+51987654321",
      "email": "maria@gmail.com"
    }
  },
  "data": {
    "amount": 150.00,
    "month": "Junio",
    "organization_name": "JASS Central"
  }
}
```