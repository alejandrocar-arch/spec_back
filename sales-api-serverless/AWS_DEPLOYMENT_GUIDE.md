# 🚀 AWS Deployment Guide - Sales API Serverless

Guía completa para validar y desplegar el proyecto serverless en AWS.

---

## 📋 Tabla de Contenidos

1. [Validación del Template SAM](#1-validación-del-template-sam)
2. [Permisos IAM Requeridos](#2-permisos-iam-requeridos)
3. [Validación de Lambdas](#3-validación-de-lambdas)
4. [Validación de Maven](#4-validación-de-maven)
5. [Ejecución Local](#5-ejecución-local)
6. [Despliegue en AWS](#6-despliegue-en-aws)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Validación del Template SAM

### ✅ Validación Automática

```bash
# Validar sintaxis del template
sam validate --lint

# Validar con AWS CloudFormation
aws cloudformation validate-template --template-body file://template.yaml
```

### ✅ Checklist Manual del Template

#### **Recursos Definidos:**
- [x] **SalesApi** (AWS::Serverless::Api)
  - Stage: Prod
  - CORS habilitado
  - Métodos: GET, POST, OPTIONS

- [x] **ProductosLambdaFunction** (AWS::Serverless::Function)
  - Runtime: java21 ✅
  - Handler: `com.supermarket.sales.serverless.productos.handler.ProductosHandler::handleRequest` ✅
  - CodeUri: `productos-lambda` ✅
  - Timeout: 30s
  - Memory: 512MB
  - Environment: PRODUCTOS_TABLE_NAME
  - Policy: DynamoDBReadPolicy

- [x] **VentasLambdaFunction** (AWS::Serverless::Function)
  - Runtime: java21 ✅
  - Handler: `com.supermarket.sales.serverless.ventas.handler.VentasHandler::handleRequest` ✅
  - CodeUri: `ventas-lambda` ✅
  - Timeout: 30s
  - Memory: 512MB
  - Environment: VENTAS_TABLE_NAME
  - Policy: DynamoDBWritePolicy

- [x] **TablaProductos** (AWS::DynamoDB::Table)
  - Partition Key: id (String)
  - GSI: CodigoBarrasIndex (codigo_barras)
  - GSI: NombreIndex (nombre)
  - Billing: PAY_PER_REQUEST
  - PITR: Enabled

- [x] **TablaVentas** (AWS::DynamoDB::Table)
  - Partition Key: ventaId (String)
  - Billing: PAY_PER_REQUEST
  - PITR: Enabled

- [x] **CloudWatch Log Groups**
  - ProductosLambdaLogGroup (7 días retention)
  - VentasLambdaLogGroup (7 días retention)

#### **API Gateway Configuration:**
```yaml
✅ Path: /productos → ProductosLambdaFunction (GET)
✅ Path: /ventas → VentasLambdaFunction (POST)
✅ CORS: Enabled
✅ Stage: Prod
```

#### **IAM Policies:**
```yaml
✅ ProductosLambda: DynamoDBReadPolicy (Query, GetItem)
✅ VentasLambda: DynamoDBWritePolicy (PutItem)
✅ Ambas: CloudWatch Logs (automático con SAM)
```

#### **Outputs:**
```yaml
✅ SalesApiUrl
✅ ProductosApiUrl
✅ VentasApiUrl
✅ ProductosLambdaArn
✅ VentasLambdaArn
✅ TablaProductosName
✅ TablaVentasName
```

### ⚠️ Posibles Problemas en Template

**Ninguno detectado** - El template está correctamente configurado.

---

## 2. Permisos IAM Requeridos

### 🔑 Permisos Mínimos para `sam build`

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::aws-sam-cli-managed-default-*/*"
    }
  ]
}
```

**Nota:** `sam build` es principalmente local, no requiere permisos AWS.

### 🔑 Permisos Mínimos para `sam deploy`

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "CloudFormationPermissions",
      "Effect": "Allow",
      "Action": [
        "cloudformation:CreateStack",
        "cloudformation:UpdateStack",
        "cloudformation:DescribeStacks",
        "cloudformation:DescribeStackEvents",
        "cloudformation:DescribeStackResources",
        "cloudformation:GetTemplate",
        "cloudformation:ValidateTemplate",
        "cloudformation:CreateChangeSet",
        "cloudformation:DescribeChangeSet",
        "cloudformation:ExecuteChangeSet",
        "cloudformation:DeleteChangeSet"
      ],
      "Resource": "arn:aws:cloudformation:*:*:stack/sales-api-serverless/*"
    },
    {
      "Sid": "S3DeploymentBucket",
      "Effect": "Allow",
      "Action": [
        "s3:CreateBucket",
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket",
        "s3:GetBucketLocation",
        "s3:GetBucketPolicy",
        "s3:PutBucketPolicy"
      ],
      "Resource": [
        "arn:aws:s3:::aws-sam-cli-managed-default-*",
        "arn:aws:s3:::aws-sam-cli-managed-default-*/*"
      ]
    },
    {
      "Sid": "LambdaPermissions",
      "Effect": "Allow",
      "Action": [
        "lambda:CreateFunction",
        "lambda:UpdateFunctionCode",
        "lambda:UpdateFunctionConfiguration",
        "lambda:GetFunction",
        "lambda:DeleteFunction",
        "lambda:AddPermission",
        "lambda:RemovePermission",
        "lambda:GetFunctionConfiguration",
        "lambda:TagResource",
        "lambda:UntagResource",
        "lambda:ListTags"
      ],
      "Resource": "arn:aws:lambda:*:*:function:sales-api-serverless-*"
    },
    {
      "Sid": "IAMPassRole",
      "Effect": "Allow",
      "Action": [
        "iam:PassRole"
      ],
      "Resource": "arn:aws:iam::*:role/sales-api-serverless-*",
      "Condition": {
        "StringEquals": {
          "iam:PassedToService": "lambda.amazonaws.com"
        }
      }
    },
    {
      "Sid": "IAMRoleManagement",
      "Effect": "Allow",
      "Action": [
        "iam:CreateRole",
        "iam:DeleteRole",
        "iam:GetRole",
        "iam:PutRolePolicy",
        "iam:DeleteRolePolicy",
        "iam:GetRolePolicy",
        "iam:AttachRolePolicy",
        "iam:DetachRolePolicy",
        "iam:TagRole",
        "iam:UntagRole"
      ],
      "Resource": "arn:aws:iam::*:role/sales-api-serverless-*"
    },
    {
      "Sid": "APIGatewayPermissions",
      "Effect": "Allow",
      "Action": [
        "apigateway:POST",
        "apigateway:GET",
        "apigateway:PUT",
        "apigateway:DELETE",
        "apigateway:PATCH"
      ],
      "Resource": "arn:aws:apigateway:*::/*"
    },
    {
      "Sid": "DynamoDBPermissions",
      "Effect": "Allow",
      "Action": [
        "dynamodb:CreateTable",
        "dynamodb:DescribeTable",
        "dynamodb:DeleteTable",
        "dynamodb:UpdateTable",
        "dynamodb:TagResource",
        "dynamodb:UntagResource",
        "dynamodb:UpdateContinuousBackups"
      ],
      "Resource": [
        "arn:aws:dynamodb:*:*:table/TablaProductos",
        "arn:aws:dynamodb:*:*:table/TablaVentas"
      ]
    },
    {
      "Sid": "CloudWatchLogsPermissions",
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:DeleteLogGroup",
        "logs:DescribeLogGroups",
        "logs:PutRetentionPolicy",
        "logs:TagLogGroup",
        "logs:UntagLogGroup"
      ],
      "Resource": "arn:aws:logs:*:*:log-group:/aws/lambda/sales-api-serverless-*"
    }
  ]
}
```

### 📝 Crear Policy IAM

```bash
# Guardar el JSON anterior como sam-deploy-policy.json

# Crear la policy
aws iam create-policy \
  --policy-name SAMDeployPolicy \
  --policy-document file://sam-deploy-policy.json

# Adjuntar al usuario
aws iam attach-user-policy \
  --user-name TU_USUARIO \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/SAMDeployPolicy
```

### ⚠️ Permisos Faltantes Comunes

Si ves estos errores, necesitas estos permisos:

| Error | Permiso Faltante |
|-------|------------------|
| `User is not authorized to perform: iam:PassRole` | `iam:PassRole` |
| `User is not authorized to perform: cloudformation:CreateStack` | CloudFormation permissions |
| `Access Denied on S3 bucket` | S3 deployment bucket permissions |
| `User is not authorized to perform: lambda:CreateFunction` | Lambda permissions |
| `User is not authorized to perform: dynamodb:CreateTable` | DynamoDB permissions |

---

## 3. Validación de Lambdas

### ✅ Handlers Correctos

**ProductosLambda:**
```
Handler: com.supermarket.sales.serverless.productos.handler.ProductosHandler::handleRequest
Clase: ProductosHandler
Método: handleRequest(APIGatewayProxyRequestEvent, Context)
Package: com.supermarket.sales.serverless.productos.handler
```

**VentasLambda:**
```
Handler: com.supermarket.sales.serverless.ventas.handler.VentasHandler::handleRequest
Clase: VentasHandler
Método: handleRequest(APIGatewayProxyRequestEvent, Context)
Package: com.supermarket.sales.serverless.ventas.handler
```

### ✅ Runtime

```yaml
Runtime: java21 ✅
```

**Verificar Java local:**
```bash
java -version
# Debe mostrar: openjdk version "21.x.x"
```

### ✅ Variables de Entorno

**ProductosLambda:**
```yaml
PRODUCTOS_TABLE_NAME: !Ref TablaProductos  ✅
```

**VentasLambda:**
```yaml
VENTAS_TABLE_NAME: !Ref TablaVentas  ✅
```

### ✅ Permisos DynamoDB

**ProductosLambda:**
```yaml
Policy: DynamoDBReadPolicy
Permite: dynamodb:Query, dynamodb:GetItem
Tabla: TablaProductos
```

**VentasLambda:**
```yaml
Policy: DynamoDBWritePolicy
Permite: dynamodb:PutItem
Tabla: TablaVentas
```

### 🔍 Verificar Estructura de Clases

```bash
# Verificar que las clases existen
ls -la sales-api-serverless/productos-lambda/src/main/java/com/supermarket/sales/serverless/productos/handler/ProductosHandler.java
ls -la sales-api-serverless/ventas-lambda/src/main/java/com/supermarket/sales/serverless/ventas/handler/VentasHandler.java
```

---

## 4. Validación de Maven

### ✅ Dependencias AWS SDK v2

**Verificar en pom.xml:**
```xml
✅ software.amazon.awssdk:dynamodb (AWS SDK v2)
✅ com.amazonaws:aws-lambda-java-core (1.2.3+)
✅ com.amazonaws:aws-lambda-java-events (3.11.4+)
✅ com.fasterxml.jackson.core:jackson-databind
✅ com.fasterxml.jackson.datatype:jackson-datatype-jsr310
```

### ✅ Java 21 Configuration

```xml
<maven.compiler.source>21</maven.compiler.source>
<maven.compiler.target>21</maven.compiler.target>
```

### ✅ Maven Shade Plugin

```xml
✅ maven-shade-plugin configurado
✅ Crea uber JAR
✅ Excluye archivos de firma (*.SF, *.DSA, *.RSA)
✅ Transformer para MANIFEST.MF
```

### 🔧 Compilar y Validar

```bash
cd sales-api-serverless

# Limpiar y compilar
mvn clean package

# Verificar JARs generados
ls -lh productos-lambda/target/*.jar
ls -lh ventas-lambda/target/*.jar

# Debe mostrar:
# productos-lambda-1.0.0.jar (uber JAR ~15-20MB)
# ventas-lambda-1.0.0.jar (uber JAR ~15-20MB)
```

### ✅ Verificar Contenido del JAR

```bash
# Ver contenido del JAR
jar tf productos-lambda/target/productos-lambda-1.0.0.jar | grep ProductosHandler

# Debe mostrar:
# com/supermarket/sales/serverless/productos/handler/ProductosHandler.class
```

---

## 5. Ejecución Local

### 📦 Prerequisitos

```bash
# Verificar herramientas
docker --version
sam --version
aws --version
mvn --version
java -version
```

### 🔨 Build Local

```bash
cd sales-api-serverless

# Build con SAM
sam build

# Verificar build
ls -la .aws-sam/build/
```

**Resultado esperado:**
```
.aws-sam/build/
├── ProductosLambdaFunction/
│   └── productos-lambda-1.0.0.jar
├── VentasLambdaFunction/
│   └── ventas-lambda-1.0.0.jar
└── template.yaml
```

### 🐳 Iniciar DynamoDB Local

```bash
# Terminal 1: DynamoDB Local
docker run -d -p 8000:8000 --name dynamodb-local amazon/dynamodb-local

# Verificar que está corriendo
docker ps | grep dynamodb-local
```

### 🗄️ Crear Tablas Locales

```bash
# Terminal 2: Setup
cd sales-api-serverless

# Crear tablas
./scripts/create-tables-local.sh

# Seed data
./scripts/seed-productos.sh
```

### 🚀 Iniciar API Local

```bash
# Terminal 3: API
sam local start-api --docker-network host

# Debe mostrar:
# Mounting ProductosLambdaFunction at http://127.0.0.1:3000/productos [GET]
# Mounting VentasLambdaFunction at http://127.0.0.1:3000/ventas [POST]
```

### 🧪 Pruebas con cURL

**Test 1: Búsqueda por código de barras**
```bash
curl "http://localhost:3000/productos?q=7501234567890"

# Esperado: 200 OK
# {
#   "id": "101",
#   "nombre": "Coca Cola 2L",
#   "codigoBarras": "7501234567890",
#   "precio": 28.50
# }
```

**Test 2: Búsqueda por nombre**
```bash
curl "http://localhost:3000/productos?q=Coca"

# Esperado: 200 OK con producto
```

**Test 3: Producto no encontrado**
```bash
curl "http://localhost:3000/productos?q=99999"

# Esperado: 404 Not Found
# {
#   "statusCode": 404,
#   "message": "Product not found",
#   "timestamp": "..."
# }
```

**Test 4: Query parameter faltante**
```bash
curl "http://localhost:3000/productos"

# Esperado: 400 Bad Request
```

**Test 5: Registrar venta**
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

# Esperado: 201 Created
# {
#   "ventaId": "uuid-generado",
#   "productoId": "101",
#   "cantidad": 2,
#   "total": 57.00,
#   "fecha": "2024-01-15T10:30:00",
#   "terminalId": "TERM-001",
#   "cashierId": "CASH-123"
# }
```

**Test 6: Venta con validación fallida**
```bash
curl -X POST http://localhost:3000/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "cashierId": "CASH-123",
    "productoId": "101",
    "cantidad": 2,
    "total": "57.00"
  }'

# Esperado: 400 Bad Request
# {
#   "statusCode": 400,
#   "message": "terminalId is required",
#   "timestamp": "..."
# }
```

---

## 6. Despliegue en AWS

### 🎯 Pre-Deployment Checklist

- [ ] AWS CLI configurado (`aws configure`)
- [ ] Credenciales válidas (`aws sts get-caller-identity`)
- [ ] Permisos IAM verificados
- [ ] Proyecto compilado (`mvn clean package`)
- [ ] SAM build exitoso (`sam build`)
- [ ] Tests locales pasados

### 🚀 Primer Despliegue (Guided)

```bash
cd sales-api-serverless

# Deploy con wizard
sam deploy --guided
```

**Responder las preguntas:**

```
Stack Name [sam-app]: sales-api-serverless
AWS Region [us-east-1]: us-east-1  (o tu región preferida)
#Shows you resources changes to be deployed and require a 'Y' to initiate deploy
Confirm changes before deploy [y/N]: y
#SAM needs permission to be able to create roles to connect to the resources in your template
Allow SAM CLI IAM role creation [Y/n]: Y
#Preserves the state of previously provisioned resources when an operation fails
Disable rollback [y/N]: N
ProductosLambdaFunction may not have authorization defined, Is this okay? [y/N]: y
VentasLambdaFunction may not have authorization defined, Is this okay? [y/N]: y
Save arguments to configuration file [Y/n]: Y
SAM configuration file [samconfig.toml]: (presionar Enter)
SAM configuration environment [default]: (presionar Enter)
```

### ⏳ Monitorear Deployment

```bash
# En otra terminal, monitorear eventos
watch -n 5 'aws cloudformation describe-stack-events \
  --stack-name sales-api-serverless \
  --max-items 10 \
  --query "StackEvents[*].[Timestamp,ResourceStatus,ResourceType,LogicalResourceId]" \
  --output table'
```

### ✅ Verificar Deployment Exitoso

```bash
# Ver status del stack
aws cloudformation describe-stacks \
  --stack-name sales-api-serverless \
  --query "Stacks[0].StackStatus"

# Debe mostrar: "CREATE_COMPLETE" o "UPDATE_COMPLETE"
```

### 📋 Obtener Outputs

```bash
# Ver todos los outputs
aws cloudformation describe-stacks \
  --stack-name sales-api-serverless \
  --query "Stacks[0].Outputs" \
  --output table

# Guardar URLs
export PRODUCTOS_URL=$(aws cloudformation describe-stacks \
  --stack-name sales-api-serverless \
  --query "Stacks[0].Outputs[?OutputKey=='ProductosApiUrl'].OutputValue" \
  --output text)

export VENTAS_URL=$(aws cloudformation describe-stacks \
  --stack-name sales-api-serverless \
  --query "Stacks[0].Outputs[?OutputKey=='VentasApiUrl'].OutputValue" \
  --output text)

echo "Productos URL: $PRODUCTOS_URL"
echo "Ventas URL: $VENTAS_URL"
```

### 🧪 Probar Endpoints en AWS

**Test 1: GET /productos**
```bash
curl "$PRODUCTOS_URL?q=test"

# Esperado: 404 (normal, no hay datos aún)
```

**Test 2: POST /ventas**
```bash
curl -X POST "$VENTAS_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId": "TERM-AWS-001",
    "cashierId": "CASH-AWS-123",
    "productoId": "AWS-101",
    "cantidad": 3,
    "total": "99.99"
  }'

# Esperado: 201 Created con ventaId
```

**Test 3: Verificar venta en DynamoDB**
```bash
aws dynamodb scan \
  --table-name TablaVentas \
  --limit 5
```

### 🔄 Despliegues Subsecuentes

```bash
# Después del primer deploy, usar:
sam build
sam deploy

# O en un solo comando:
sam build && sam deploy
```

### 📊 Monitorear Logs

```bash
# Logs de ProductosLambda
sam logs -n ProductosLambdaFunction --stack-name sales-api-serverless --tail

# Logs de VentasLambda
sam logs -n VentasLambdaFunction --stack-name sales-api-serverless --tail

# O con AWS CLI
aws logs tail /aws/lambda/sales-api-serverless-ProductosLambdaFunction-XXXXX --follow
```

---

## 7. Troubleshooting

### ❌ Error: "Unable to upload artifact"

**Causa:** Permisos S3 insuficientes

**Solución:**
```bash
# Verificar bucket SAM
aws s3 ls | grep sam-cli-managed

# Crear bucket manualmente si es necesario
aws s3 mb s3://aws-sam-cli-managed-default-samclisourcebucket-XXXXX
```

### ❌ Error: "User is not authorized to perform: iam:PassRole"

**Causa:** Falta permiso IAM PassRole

**Solución:**
```json
{
  "Effect": "Allow",
  "Action": "iam:PassRole",
  "Resource": "arn:aws:iam::*:role/sales-api-serverless-*",
  "Condition": {
    "StringEquals": {
      "iam:PassedToService": "lambda.amazonaws.com"
    }
  }
}
```

### ❌ Error: "Stack sales-api-serverless already exists"

**Causa:** Stack ya existe

**Solución:**
```bash
# Actualizar stack existente
sam deploy

# O eliminar y recrear
aws cloudformation delete-stack --stack-name sales-api-serverless
aws cloudformation wait stack-delete-complete --stack-name sales-api-serverless
sam deploy --guided
```

### ❌ Error: "BUILD FAILED" en Maven

**Causa:** Java version incorrecta

**Solución:**
```bash
# Verificar Java
java -version

# Debe ser Java 21+
# Si no, instalar Java 21 y configurar JAVA_HOME
```

### ❌ Error: "Handler not found"

**Causa:** Handler path incorrecto en template.yaml

**Solución:**
```bash
# Verificar que la clase existe
ls -la productos-lambda/src/main/java/com/supermarket/sales/serverless/productos/handler/ProductosHandler.java

# Verificar handler en template.yaml
# Debe ser: com.supermarket.sales.serverless.productos.handler.ProductosHandler::handleRequest
```

### ❌ Error: "Table already exists"

**Causa:** Tablas DynamoDB ya existen

**Solución:**
```bash
# Opción 1: Eliminar tablas
aws dynamodb delete-table --table-name TablaProductos
aws dynamodb delete-table --table-name TablaVentas

# Opción 2: Cambiar nombres en template.yaml
```

### ❌ Error: Lambda timeout

**Causa:** Lambda tarda más de 30s

**Solución:**
```yaml
# Aumentar timeout en template.yaml
Globals:
  Function:
    Timeout: 60  # Aumentar a 60s
```

### ❌ Error: "Cannot connect to DynamoDB" (local)

**Causa:** DynamoDB Local no está corriendo o network issue

**Solución:**
```bash
# Verificar DynamoDB Local
docker ps | grep dynamodb-local

# Usar --docker-network host
sam local start-api --docker-network host

# O reiniciar DynamoDB Local
docker restart dynamodb-local
```

### 📞 Obtener Ayuda

```bash
# Ver logs detallados de SAM
sam deploy --debug

# Ver logs de CloudFormation
aws cloudformation describe-stack-events \
  --stack-name sales-api-serverless \
  --max-items 50

# Ver logs de Lambda
aws logs tail /aws/lambda/sales-api-serverless-ProductosLambdaFunction-XXXXX
```

---

## 🎯 Checklist Final

### Pre-Deployment
- [ ] Java 21 instalado
- [ ] Maven 3.8+ instalado
- [ ] AWS CLI configurado
- [ ] SAM CLI instalado
- [ ] Docker instalado (para local)
- [ ] Permisos IAM verificados

### Build
- [ ] `mvn clean package` exitoso
- [ ] `sam build` exitoso
- [ ] JARs generados correctamente

### Local Testing
- [ ] DynamoDB Local corriendo
- [ ] Tablas creadas localmente
- [ ] `sam local start-api` funciona
- [ ] Tests con curl pasados

### AWS Deployment
- [ ] `sam deploy --guided` exitoso
- [ ] Stack status: CREATE_COMPLETE
- [ ] Outputs obtenidos
- [ ] Endpoints probados
- [ ] Logs visibles en CloudWatch

### Post-Deployment
- [ ] API Gateway funciona
- [ ] Lambdas responden correctamente
- [ ] DynamoDB guarda datos
- [ ] Logs se generan correctamente

---

## 📚 Recursos Adicionales

- [AWS SAM Documentation](https://docs.aws.amazon.com/serverless-application-model/)
- [AWS Lambda Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)
- [DynamoDB Developer Guide](https://docs.aws.amazon.com/dynamodb/)
- [API Gateway Documentation](https://docs.aws.amazon.com/apigateway/)

---

**¡Listo para desplegar! 🚀**
