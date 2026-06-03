# ⚙️ Sales API Backend (POS)
Repositorio backend del POS. Incluye una implementación serverless activa en AWS y una implementación Spring Boot tradicional de referencia.

## Contenido del repositorio
- `sales-api-serverless/` ✅ **Implementación activa** (AWS SAM + Lambda + DynamoDB)
- `src/` + `pom.xml` (raíz) ℹ️ implementación Spring Boot tradicional

Si vas a desplegar/usar el backend conectado al frontend actual, usa `sales-api-serverless/`.

## Arquitectura serverless (actual)
- API Gateway
  - `GET /productos`
  - `POST /ventas`
- `ProductosLambda` (Java 21)
- `VentasLambda` (Java 21)
- DynamoDB
  - `TablaProductos` (PK `id`, GSI por `codigo_barras`, GSI por `nombre`)
  - `TablaVentas` (PK `ventaId`)

## Requisitos
- Java 21
- Maven 3.8+
- AWS SAM CLI
- AWS CLI configurado con credenciales válidas
- Docker (opcional para pruebas locales con SAM Local)

## Deploy en AWS (serverless)
```bash
cd sales-api-serverless
mvn clean package
sam build
sam deploy --guided
```

Para despliegues siguientes:
```bash
sam deploy
```

## Prueba rápida en AWS
Obtén la URL de outputs del stack y prueba:

```bash
curl "<PRODUCTOS_API_URL>?q=7509990001112"
```

```bash
curl -X POST "<VENTAS_API_URL>" \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId":"TERM-001",
    "cashierId":"CASH-001",
    "productoId":"E2E-PROD-001",
    "cantidad":1,
    "total":"28.50"
  }'
```

## Desarrollo local (serverless)
```bash
cd sales-api-serverless
sam build
sam local start-api
```

## Modelo de datos DynamoDB (actual)
### TablaProductos
- PK: `id` (string)
- Atributos usados por lambdas/frontend: `nombre`, `codigo_barras`, `precio`
- Atributos opcionales: `categoria`, `descripcion`, `stock`, `unidad`
- Índices:
  - `CodigoBarrasIndex`
  - `NombreIndex`

### TablaVentas
- PK: `ventaId`
- Registro de venta con terminal, cajero, producto, cantidad, total y fecha

## Nota sobre modelo `id + details (JSON)`
Es viable en DynamoDB, pero si eliminas campos indexables de nivel superior (`codigo_barras`, `nombre`) perderás eficiencia de búsqueda y terminarás en scans más costosos. Para operación real de POS, se recomienda mantener atributos indexables.

## Estructura rápida
```text
spec_back/
  sales-api-serverless/
    productos-lambda/
    ventas-lambda/
    template.yaml
  src/                # Spring Boot (referencia)
  pom.xml             # Spring Boot (referencia)
```

## Flujo Kiro / Spec-Driven
Para mantener trazabilidad y consistencia con Kiro:

- Requerimientos primero (`requirements.md`)
- Diseño técnico después (`design.md`)
- Ejecución por tareas (`tasks.md`)

Ruta de referencia en este repo:
- `.kiro/specs/pos-api/requirements.md`
- `.kiro/specs/pos-api/design.md`
- `.kiro/specs/pos-api/tasks.md`
