# 🚀 Sales API Serverless - POS System V1

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![AWS SAM](https://img.shields.io/badge/AWS%20SAM-1.100+-yellow.svg)](https://aws.amazon.com/serverless/sam/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Versión serverless simplificada del sistema POS de supermercado utilizando AWS SAM (Serverless Application Model). Esta V1 se enfoca en dos funcionalidades core: **búsqueda de productos** y **registro de ventas**.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Prerequisitos](#-prerequisitos)
- [Instalación](#-instalación)
- [Desarrollo Local](#-desarrollo-local)
- [Despliegue en AWS](#-despliegue-en-aws)
- [Uso de la API](#-uso-de-la-api)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Características

### Búsqueda de Productos
- 🔍 Búsqueda por código de barras (query numérico)
- 📝 Búsqueda por nombre de producto (query alfabético)
- ⚡ Consultas rápidas usando DynamoDB GSIs

### Registro de Ventas
- 💰 Registro de ventas con validación
- 🆔 Generación automática de ID único (UUID)
- 📅 Timestamp automático en UTC
- 💾 Almacenamiento persistente en DynamoDB

### Características Técnicas
- ☁️ Arquitectura 100% serverless
- 🔐 IAM roles con permisos mínimos (least privilege)
- 📊 Logs estructurados en CloudWatch
- 🌐 API REST con CORS habilitado
- 📦 Maven multi-módulo para builds independientes

---

## 🏗️ Arquitectura

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│         API Gateway (REST)          │
│  GET /productos  │  POST /ventas    │
└────────┬─────────┴──────────┬───────┘
         │                    │
         ▼                    ▼
┌──────────────────┐  ┌──────────────────┐
│ ProductosLambda  │  │  VentasLambda    │
│    (Java 21)     │  │    (Java 21)     │
└────────┬─────────┘  └────────┬─────────┘
         │                     │
         ▼                     ▼
┌──────────────────┐  ┌──────────────────┐
│ TablaProductos   │  │  TablaVentas     │
│   (DynamoDB)     │  │   (DynamoDB)     │
│  + 2 GSIs        │  │                  │
└──────────────────┘  └──────────────────┘
```

### Componentes

| Componente | Tecnología | Propósito |
|------------|------------|-----------|
| API Gateway | AWS API Gateway | Expone endpoints HTTP REST |
| ProductosLambda | AWS Lambda (Java 21) | Maneja búsqueda de productos |
| VentasLambda | AWS Lambda (Java 21) | Maneja registro de ventas |
| TablaProductos | DynamoDB | Almacena productos con GSIs |
| TablaVentas | DynamoDB | Almacena ventas registradas |

---

## 📦 Prerequisitos

Antes de comenzar, asegúrate de tener instalado:

### Requerido
- ☕ **Java 21** o superior
  ```bash
  java -version
  ```

- 📦 **Maven 3.8+**
  ```bash
  mvn -version
  ```

- 🔧 **AWS SAM CLI 1.100+**
  ```bash
  sam --version
  ```

- 🐳 **Docker** (para testing local)
  ```bash
  docker --version
  ```

### Opcional
- 🔑 **AWS CLI** (para deployment)
  ```bash
  aws --version
  ```

---

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd sales-api-serverless
```

### 2. Compilar el Proyecto

```bash
mvn clean package
```

Este comando:
- Descarga todas las dependencias
- Compila ambos módulos Lambda
- Genera los uber JARs con maven-shade-plugin

### 3. Build con SAM

```bash
sam build
```

Este comando:
- Prepara los deployment packages
- Copia los JARs a `.aws-sam/build/`
- Prepara la infraestructura para deployment

---

## 💻 Desarrollo Local

### Iniciar DynamoDB Local

```bash
docker run -p 8000:8000 amazon/dynamodb-local
```

### Crear Tablas Locales

```bash
# Crear TablaProductos
aws dynamodb create-table \
  --table-name TablaProductos \
  --attribute-definitions \
    AttributeName=id,AttributeType=S \
    AttributeName=codigo_barras,AttributeType=S \
    AttributeName=nombre,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --global-secondary-indexes \
    "[{\"IndexName\":\"CodigoBarrasIndex\",\"KeySchema\":[{\"AttributeName\":\"codigo_barras\",\"KeyType\":\"HASH\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}},{\"IndexName\":\"NombreIndex\",\"KeySchema\":[{\"AttributeName\":\"nombre\",\"KeyType\":\"HASH\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}}]" \
  --endpoint-url http://localhost:8000

# Crear TablaVentas
aws dynamodb create-table \
  --table-name TablaVentas \
  --attribute-definitions AttributeName=ventaId,AttributeType=S \
  --key-schema AttributeName=ventaId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url http://localhost:8000
```

### Seed Data de Prueba

```bash
# Insertar producto de prueba
aws dynamodb put-item \
  --table-name TablaProductos \
  --item '{
    "id": {"S": "101"},
    "nombre": {"S": "Coca Cola 2L"},
    "codigo_barras": {"S": "7501234567890"},
    "precio": {"S": "28.50"}
  }' \
  --endpoint-url http://localhost:8000
```

### Iniciar API Local

```bash
sam local start-api --docker-network host
```

La API estará disponible en: **http://localhost:3000**

### Probar Endpoints Localmente

**Búsqueda por código de barras:**
```bash
curl "http://localhost:3000/productos?q=7501234567890"
```

**Búsqueda por nombre:**
```bash
curl "http://localhost:3000/productos?q=Coca"
```

**Registrar venta:**
```bash
curl -X POST http://localhost:3000/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId": "TERM-001",
    "cashierId": "CASH-123",
    "productoId": "101",
    "cantidad": 2,
    "total": "57.00"
  }'
```

---

## ☁️ Despliegue en AWS

### Primer Despliegue (Guided)

```bash
sam deploy --guided
```

Responde las preguntas:
- **Stack Name**: `sales-api-serverless`
- **AWS Region**: `us-east-1` (o tu región preferida)
- **Confirm changes**: `Y`
- **Allow SAM CLI IAM role creation**: `Y`
- **Disable rollback**: `N`
- **Save arguments to configuration file**: `Y`

### Despliegues Subsecuentes

```bash
sam deploy
```

### Obtener URLs de Endpoints

```bash
aws cloudformation describe-stacks \
  --stack-name sales-api-serverless \
  --query "Stacks[0].Outputs"
```

O simplemente revisa la salida del comando `sam deploy`.

---

## 🔌 Uso de la API

### GET /productos

**Búsqueda por código de barras (numérico):**
```bash
curl "https://<api-id>.execute-api.<region>.amazonaws.com/Prod/productos?q=7501234567890"
```

**Respuesta exitosa (200):**
```json
{
  "id": "101",
  "nombre": "Coca Cola 2L",
  "codigoBarras": "7501234567890",
  "precio": 28.50
}
```

**Búsqueda por nombre (alfabético):**
```bash
curl "https://<api-id>.execute-api.<region>.amazonaws.com/Prod/productos?q=Coca"
```

**Producto no encontrado (404):**
```json
{
  "statusCode": 404,
  "message": "Product not found",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Query parameter faltante (400):**
```json
{
  "statusCode": 400,
  "message": "Query parameter 'q' is required",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### POST /ventas

**Registrar venta:**
```bash
curl -X POST "https://<api-id>.execute-api.<region>.amazonaws.com/Prod/ventas" \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId": "TERM-001",
    "cashierId": "CASH-123",
    "productoId": "101",
    "cantidad": 2,
    "total": "57.00"
  }'
```

**Respuesta exitosa (201):**
```json
{
  "ventaId": "550e8400-e29b-41d4-a716-446655440000",
  "productoId": "101",
  "cantidad": 2,
  "total": 57.00,
  "fecha": "2024-01-15T10:30:00",
  "terminalId": "TERM-001",
  "cashierId": "CASH-123"
}
```

**Error de validación (400):**
```json
{
  "statusCode": 400,
  "message": "terminalId is required",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 📁 Estructura del Proyecto

```
sales-api-serverless/
├── template.yaml                           # SAM template (IaC)
├── pom.xml                                 # Root Maven POM
├── README.md                               # Este archivo
│
├── productos-lambda/
│   ├── pom.xml                             # Lambda-specific POM
│   └── src/main/java/.../productos/
│       ├── handler/
│       │   └── ProductosHandler.java       # Lambda entry point
│       ├── service/
│       │   └── ProductSearchService.java   # Business logic
│       ├── repository/
│       │   └── ProductRepository.java      # DynamoDB access
│       ├── dto/
│       │   ├── ProductDTO.java
│       │   └── ErrorResponse.java
│       └── exception/
│           └── ProductNotFoundException.java
│
└── ventas-lambda/
    ├── pom.xml                             # Lambda-specific POM
    └── src/main/java/.../ventas/
        ├── handler/
        │   └── VentasHandler.java          # Lambda entry point
        ├── service/
        │   └── SaleRegistrationService.java # Business logic
        ├── repository/
        │   └── SaleRepository.java         # DynamoDB access
        ├── dto/
        │   ├── CreateSaleRequest.java
        │   ├── SaleDTO.java
        │   └── ErrorResponse.java
        └── exception/
            └── ValidationException.java
```

---

## 🧪 Testing

### Unit Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un módulo específico
mvn test -pl productos-lambda
mvn test -pl ventas-lambda
```

### Integration Tests

```bash
# Requiere DynamoDB Local corriendo
mvn verify
```

### Testing Manual con SAM Local

```bash
# 1. Iniciar DynamoDB Local
docker run -p 8000:8000 amazon/dynamodb-local

# 2. Crear tablas y seed data (ver sección Desarrollo Local)

# 3. Iniciar API
sam local start-api --docker-network host

# 4. Probar endpoints con curl o Postman
```

---

## 🔍 Troubleshooting

### Error: "PRODUCTOS_TABLE_NAME environment variable is not set"

**Causa**: La variable de entorno no está configurada en el template.yaml

**Solución**: Verifica que el template.yaml tenga:
```yaml
Environment:
  Variables:
    PRODUCTOS_TABLE_NAME: !Ref TablaProductos
```

### Error: "Unable to connect to DynamoDB"

**Causa**: DynamoDB Local no está corriendo o la Lambda no puede conectarse

**Solución para local**:
```bash
# Usar --docker-network host
sam local start-api --docker-network host
```

**Solución para AWS**: Verifica que los IAM roles tengan permisos correctos.

### Error: "Maven build failed"

**Causa**: Java version incorrecta o dependencias faltantes

**Solución**:
```bash
# Verificar Java version
java -version  # Debe ser 21+

# Limpiar y rebuild
mvn clean install -U
```

### Logs de CloudWatch

```bash
# Ver logs de ProductosLambda
sam logs -n ProductosLambdaFunction --stack-name sales-api-serverless --tail

# Ver logs de VentasLambda
sam logs -n VentasLambdaFunction --stack-name sales-api-serverless --tail
```

---

## 📝 Notas Importantes

### V1 Scope
Esta es una versión V1 simplificada que **NO incluye**:
- ❌ Autenticación/Autorización
- ❌ Frontend o UI
- ❌ Integración con APIs externas
- ❌ Retry logic o circuit breakers
- ❌ Funcionalidades avanzadas del POS (descuentos, devoluciones, etc.)

### Costos AWS
- **DynamoDB**: Billing mode on-demand (pagas por request)
- **Lambda**: Free tier incluye 1M requests/mes
- **API Gateway**: Free tier incluye 1M requests/mes
- **CloudWatch Logs**: Retención de 7 días

### Seguridad
- IAM roles siguen el principio de menor privilegio
- ProductosLambda: Solo lectura en TablaProductos
- VentasLambda: Solo escritura en TablaVentas
- CORS habilitado para desarrollo (ajustar para producción)

---

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia Apache 2.0.

---

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

---

**Hecho con ❤️ usando AWS SAM y Java 21**
