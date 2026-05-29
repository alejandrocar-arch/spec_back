# Validation Checklist - Sales API Serverless

Este documento contiene los checkpoints de validación para el proyecto serverless.

## ✅ Checkpoint 1: Validar ProductosLambda Localmente

### Prerequisitos
- [ ] Docker instalado y corriendo
- [ ] AWS CLI instalado
- [ ] SAM CLI instalado
- [ ] Maven instalado

### Pasos de Validación

#### 1. Compilar el Proyecto
```bash
cd sales-api-serverless
mvn clean package
```

**Resultado esperado:** Build exitoso sin errores

#### 2. Build con SAM
```bash
sam build
```

**Resultado esperado:** 
- ProductosLambda build exitoso
- VentasLambda build exitoso
- Artifacts en `.aws-sam/build/`

#### 3. Iniciar DynamoDB Local
```bash
docker run -d -p 8000:8000 --name dynamodb-local amazon/dynamodb-local
```

**Resultado esperado:** Container corriendo en puerto 8000

#### 4. Crear Tablas Locales
```bash
./scripts/create-tables-local.sh
```

**Resultado esperado:**
- TablaProductos creada con GSIs
- TablaVentas creada

#### 5. Seed Data de Prueba
```bash
./scripts/seed-productos.sh
```

**Resultado esperado:** 6 productos insertados en TablaProductos

#### 6. Iniciar API Local
```bash
sam local start-api --docker-network host
```

**Resultado esperado:** API corriendo en http://localhost:3000

#### 7. Probar GET /productos con Código de Barras
```bash
curl "http://localhost:3000/productos?q=7501234567890"
```

**Resultado esperado:**
- Status: 200 OK
- Body: JSON con producto Coca Cola 2L
```json
{
  "id": "101",
  "nombre": "Coca Cola 2L",
  "codigoBarras": "7501234567890",
  "precio": 28.50
}
```

#### 8. Probar GET /productos con Nombre
```bash
curl "http://localhost:3000/productos?q=Coca"
```

**Resultado esperado:**
- Status: 200 OK
- Body: JSON con producto que contiene "Coca" en el nombre

#### 9. Probar GET /productos con Producto Inexistente
```bash
curl "http://localhost:3000/productos?q=99999"
```

**Resultado esperado:**
- Status: 404 Not Found
- Body: Error response
```json
{
  "statusCode": 404,
  "message": "Product not found",
  "timestamp": "..."
}
```

#### 10. Probar GET /productos sin Query Parameter
```bash
curl "http://localhost:3000/productos"
```

**Resultado esperado:**
- Status: 400 Bad Request
- Body: Validation error
```json
{
  "statusCode": 400,
  "message": "Query parameter 'q' is required",
  "timestamp": "..."
}
```

### Resultado del Checkpoint 1
- [ ] Todos los tests pasaron exitosamente
- [ ] ProductosLambda funciona correctamente en local
- [ ] Listo para continuar con VentasLambda

---

## ✅ Checkpoint 2: Validar VentasLambda Localmente

### Prerequisitos
- [ ] Checkpoint 1 completado
- [ ] DynamoDB Local corriendo
- [ ] SAM local API corriendo

### Pasos de Validación

#### 1. Compilar ventas-lambda
```bash
mvn clean package -pl ventas-lambda
```

**Resultado esperado:** Build exitoso

#### 2. Rebuild con SAM
```bash
sam build
```

**Resultado esperado:** VentasLambda build exitoso

#### 3. Probar POST /ventas con Datos Válidos
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

**Resultado esperado:**
- Status: 201 Created
- Body: JSON con venta registrada
```json
{
  "ventaId": "uuid-generado",
  "productoId": "101",
  "cantidad": 2,
  "total": 57.00,
  "fecha": "2024-01-15T10:30:00",
  "terminalId": "TERM-001",
  "cashierId": "CASH-123"
}
```

#### 4. Probar POST /ventas con JSON Inválido
```bash
curl -X POST http://localhost:3000/ventas \
  -H "Content-Type: application/json" \
  -d '{ invalid json }'
```

**Resultado esperado:**
- Status: 400 Bad Request
- Body: Error de validación

#### 5. Probar POST /ventas sin terminalId
```bash
curl -X POST http://localhost:3000/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "cashierId": "CASH-123",
    "productoId": "101",
    "cantidad": 2,
    "total": "57.00"
  }'
```

**Resultado esperado:**
- Status: 400 Bad Request
- Message: "terminalId is required"

#### 6. Verificar Venta en DynamoDB Local
```bash
aws dynamodb scan \
  --table-name TablaVentas \
  --endpoint-url http://localhost:8000
```

**Resultado esperado:** Venta registrada visible en la tabla

### Resultado del Checkpoint 2
- [ ] Todos los tests pasaron exitosamente
- [ ] VentasLambda funciona correctamente en local
- [ ] Ventas se guardan correctamente en DynamoDB
- [ ] Listo para deployment en AWS

---

## ✅ Checkpoint 3: Validar Deployment en AWS

### Prerequisitos
- [ ] Checkpoints 1 y 2 completados
- [ ] AWS CLI configurado con credenciales
- [ ] Permisos IAM para crear recursos

### Pasos de Validación

#### 1. Primer Deployment
```bash
sam deploy --guided
```

**Configuración:**
- Stack Name: `sales-api-serverless`
- AWS Region: `us-east-1` (o tu región)
- Confirm changes: `Y`
- Allow SAM CLI IAM role creation: `Y`
- Disable rollback: `N`
- Save arguments: `Y`

**Resultado esperado:**
- Stack status: CREATE_COMPLETE
- Outputs mostrados en consola

#### 2. Capturar URLs de Endpoints
```bash
aws cloudformation describe-stacks \
  --stack-name sales-api-serverless \
  --query "Stacks[0].Outputs"
```

**Resultado esperado:** URLs de ProductosApiUrl y VentasApiUrl

#### 3. Probar GET /productos en AWS
```bash
curl "https://<api-id>.execute-api.<region>.amazonaws.com/Prod/productos?q=test"
```

**Resultado esperado:**
- Status: 404 (normal, no hay datos aún) o 200 si hay datos

#### 4. Probar POST /ventas en AWS
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

**Resultado esperado:**
- Status: 201 Created
- Venta registrada con ventaId

#### 5. Verificar Logs en CloudWatch
```bash
# ProductosLambda logs
sam logs -n ProductosLambdaFunction --stack-name sales-api-serverless --tail

# VentasLambda logs
sam logs -n VentasLambdaFunction --stack-name sales-api-serverless --tail
```

**Resultado esperado:** Logs visibles con request/response details

#### 6. Verificar Tablas DynamoDB
```bash
aws dynamodb list-tables
```

**Resultado esperado:**
- TablaProductos existe
- TablaVentas existe

#### 7. Verificar Funciones Lambda
```bash
aws lambda list-functions --query "Functions[?starts_with(FunctionName, 'sales-api-serverless')]"
```

**Resultado esperado:**
- ProductosLambdaFunction existe
- VentasLambdaFunction existe

### Resultado del Checkpoint 3
- [ ] Deployment exitoso en AWS
- [ ] Ambos endpoints responden correctamente
- [ ] Logs se generan en CloudWatch
- [ ] Tablas DynamoDB creadas correctamente

---

## ✅ Checkpoint Final: Validación Completa del Sistema

### Validaciones de Infraestructura

#### 1. Verificar API Gateway
```bash
aws apigateway get-rest-apis
```

**Verificar:**
- [ ] API "sales-api-serverless" existe
- [ ] Stage "Prod" configurado

#### 2. Verificar IAM Roles
```bash
aws iam list-roles --query "Roles[?contains(RoleName, 'sales-api-serverless')]"
```

**Verificar:**
- [ ] ProductosLambda role tiene permisos de lectura DynamoDB
- [ ] VentasLambda role tiene permisos de escritura DynamoDB
- [ ] Ambos roles tienen permisos CloudWatch Logs

#### 3. Verificar Configuración DynamoDB

**TablaProductos:**
```bash
aws dynamodb describe-table --table-name TablaProductos
```

**Verificar:**
- [ ] Billing mode: PAY_PER_REQUEST
- [ ] Point-in-time recovery: ENABLED
- [ ] GSI "CodigoBarrasIndex" existe
- [ ] GSI "NombreIndex" existe

**TablaVentas:**
```bash
aws dynamodb describe-table --table-name TablaVentas
```

**Verificar:**
- [ ] Billing mode: PAY_PER_REQUEST
- [ ] Point-in-time recovery: ENABLED

#### 4. Verificar CloudWatch Logs
```bash
aws logs describe-log-groups --log-group-name-prefix "/aws/lambda/sales-api-serverless"
```

**Verificar:**
- [ ] Log group para ProductosLambda existe
- [ ] Log group para VentasLambda existe
- [ ] Retention: 7 días

### Validaciones Funcionales

#### 1. Test End-to-End: Búsqueda de Producto
```bash
# Insertar producto de prueba
aws dynamodb put-item \
  --table-name TablaProductos \
  --item '{
    "id": {"S": "TEST-001"},
    "nombre": {"S": "Producto Test"},
    "codigo_barras": {"S": "1234567890"},
    "precio": {"S": "10.00"}
  }'

# Buscar por código de barras
curl "https://<api-url>/productos?q=1234567890"

# Buscar por nombre
curl "https://<api-url>/productos?q=Producto"
```

**Verificar:**
- [ ] Búsqueda por código de barras funciona
- [ ] Búsqueda por nombre funciona
- [ ] Respuestas JSON correctas

#### 2. Test End-to-End: Registro de Venta
```bash
curl -X POST "https://<api-url>/ventas" \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId": "TERM-TEST",
    "cashierId": "CASH-TEST",
    "productoId": "TEST-001",
    "cantidad": 1,
    "total": "10.00"
  }'

# Verificar en DynamoDB
aws dynamodb scan --table-name TablaVentas --limit 5
```

**Verificar:**
- [ ] Venta se registra correctamente
- [ ] ventaId es UUID válido
- [ ] Fecha en formato ISO 8601
- [ ] Datos guardados en DynamoDB

### Resultado del Checkpoint Final
- [ ] Toda la infraestructura está correctamente configurada
- [ ] Ambos endpoints funcionan end-to-end
- [ ] IAM roles siguen principio de menor privilegio
- [ ] Logs y monitoreo funcionan correctamente
- [ ] Sistema listo para uso en producción

---

## 📋 Resumen de Validación

| Checkpoint | Estado | Notas |
|------------|--------|-------|
| 1. ProductosLambda Local | ⏳ Pendiente | |
| 2. VentasLambda Local | ⏳ Pendiente | |
| 3. Deployment AWS | ⏳ Pendiente | |
| 4. Validación Final | ⏳ Pendiente | |

---

## 🐛 Troubleshooting

### Problema: DynamoDB Local no responde
**Solución:**
```bash
docker ps  # Verificar que está corriendo
docker logs dynamodb-local  # Ver logs
docker restart dynamodb-local  # Reiniciar si es necesario
```

### Problema: SAM local no encuentra las tablas
**Solución:**
```bash
# Usar --docker-network host
sam local start-api --docker-network host
```

### Problema: Build de Maven falla
**Solución:**
```bash
# Verificar Java version
java -version  # Debe ser 21+

# Limpiar y rebuild
mvn clean install -U
```

### Problema: Lambda timeout en AWS
**Solución:**
- Verificar logs en CloudWatch
- Aumentar timeout en template.yaml si es necesario
- Verificar conectividad con DynamoDB

---

**Última actualización:** 2024-01-15
