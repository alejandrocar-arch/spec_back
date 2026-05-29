# Product Attributes Guide

## Overview

ProductRepository and ProductDTO have been restructured to support additional optional product attributes without breaking existing GSI indexes.

## DynamoDB Item Structure

### Required Attributes (GSI-indexed)
- `id` (String, PK) - Product unique identifier
- `nombre` (String) - Product name, indexed by **NombreIndex** GSI
- `codigo_barras` (String) - Barcode, indexed by **CodigoBarrasIndex** GSI
- `precio` (Number) - Product price

### Optional Attributes (Root-level)
- `categoria` (String) - Product category (e.g., "lacteos", "bebidas")
- `descripcion` (String) - Product description
- `stock` (Number) - Available stock quantity
- `unidad` (String) - Unit of measure (e.g., "unidad", "kg", "litro")

## DynamoDB Item Examples

### Minimal Product (Required fields only)
```json
{
  "id": {"S": "1"},
  "nombre": {"S": "Coca Cola"},
  "codigo_barras": {"S": "7501234567890"},
  "precio": {"N": "2850"}
}
```

### Complete Product (All fields)
```json
{
  "id": {"S": "1"},
  "nombre": {"S": "Leche Entera"},
  "codigo_barras": {"S": "7702001"},
  "precio": {"N": "3500"},
  "categoria": {"S": "lacteos"},
  "descripcion": {"S": "Leche entera pasteurizada 1L"},
  "stock": {"N": "100"},
  "unidad": {"S": "litro"}
}
```

## API Response Examples

### GET /productos?q=7702001 (Complete product)
```json
{
  "id": "1",
  "nombre": "Leche Entera",
  "codigoBarras": "7702001",
  "precio": 35.00,
  "categoria": "lacteos",
  "descripcion": "Leche entera pasteurizada 1L",
  "stock": 100,
  "unidad": "litro"
}
```

### GET /productos?q=7501234567890 (Minimal product)
```json
{
  "id": "1",
  "nombre": "Coca Cola",
  "codigoBarras": "7501234567890",
  "precio": 28.50,
  "categoria": null,
  "descripcion": null,
  "stock": null,
  "unidad": null
}
```

## Implementation Details

### ProductDTO.java
- Added 4 optional fields: `categoria`, `descripcion`, `stock`, `unidad`
- All optional fields are nullable
- Jackson serialization includes null values in JSON response
- Two constructors: minimal (4 params) and complete (8 params)

### ProductRepository.java
- Updated `mapItemToProductDTO()` method with safe read pattern
- Uses `item.containsKey("field")` to check if optional attribute exists
- Returns `null` for missing optional attributes (no NullPointerException)
- Example safe read:
  ```java
  String categoria = item.containsKey("categoria") ? item.get("categoria").s() : null;
  Integer stock = item.containsKey("stock") ? Integer.parseInt(item.get("stock").n()) : null;
  ```

## GSI Indexes (Unchanged)

### CodigoBarrasIndex
- Partition Key: `codigo_barras` (String)
- Projection: ALL
- Used by: `queryByCodigoBarras(String codigoBarras)`

### NombreIndex
- Partition Key: `nombre` (String)
- Projection: ALL
- Used by: `queryByNombre(String nombre)`

**Important**: GSI indexes remain unchanged and continue to work with both minimal and complete products.

## Seeding Test Data

### AWS CLI - Insert Complete Product
```bash
aws dynamodb put-item \
  --table-name TablaProductos \
  --item '{
    "id": {"S": "1"},
    "nombre": {"S": "Leche Entera"},
    "codigo_barras": {"S": "7702001"},
    "precio": {"N": "3500"},
    "categoria": {"S": "lacteos"},
    "descripcion": {"S": "Leche entera pasteurizada 1L"},
    "stock": {"N": "100"},
    "unidad": {"S": "litro"}
  }'
```

### AWS CLI - Insert Minimal Product
```bash
aws dynamodb put-item \
  --table-name TablaProductos \
  --item '{
    "id": {"S": "2"},
    "nombre": {"S": "Coca Cola"},
    "codigo_barras": {"S": "7501234567890"},
    "precio": {"N": "2850"}
  }'
```

### DynamoDB Local (port 8000)
```bash
aws dynamodb put-item \
  --table-name TablaProductos \
  --endpoint-url http://localhost:8000 \
  --item '{
    "id": {"S": "1"},
    "nombre": {"S": "Leche Entera"},
    "codigo_barras": {"S": "7702001"},
    "precio": {"N": "3500"},
    "categoria": {"S": "lacteos"},
    "descripcion": {"S": "Leche entera pasteurizada 1L"},
    "stock": {"N": "100"},
    "unidad": {"S": "litro"}
  }'
```

## Testing

### Test with Complete Product
```bash
# Insert complete product
aws dynamodb put-item --table-name TablaProductos --item '{"id":{"S":"1"},"nombre":{"S":"Leche"},"codigo_barras":{"S":"7702001"},"precio":{"N":"3500"},"categoria":{"S":"lacteos"},"stock":{"N":"100"}}'

# Query by barcode
curl "https://your-api-url/productos?q=7702001"

# Expected: All fields present (categoria, stock, etc.)
```

### Test with Minimal Product
```bash
# Insert minimal product
aws dynamodb put-item --table-name TablaProductos --item '{"id":{"S":"2"},"nombre":{"S":"Coca Cola"},"codigo_barras":{"S":"7501234567890"},"precio":{"N":"2850"}}'

# Query by barcode
curl "https://your-api-url/productos?q=7501234567890"

# Expected: Optional fields are null
```

## Deployment

After making these changes, redeploy:

```bash
cd sales-api-serverless

# Clean and rebuild
mvn clean package -DskipTests

# SAM build
sam build

# Deploy (use existing stack)
sam deploy
```

## Backward Compatibility

✅ **Existing products without optional attributes continue to work**
- GSI indexes remain functional
- API returns null for missing optional fields
- No migration required for existing data

✅ **New products can include optional attributes**
- Add attributes as needed
- GSI indexes automatically include new attributes (Projection: ALL)

## Notes

- All attributes are stored at the root level (not nested)
- DynamoDB cannot index nested attributes, so GSIs require root-level attributes
- Optional attributes use safe read pattern to avoid NullPointerException
- Jackson serialization includes null values in JSON response
- Price is stored as Number in DynamoDB, mapped to BigDecimal in Java
