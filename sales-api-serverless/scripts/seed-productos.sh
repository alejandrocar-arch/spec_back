#!/bin/bash

# Script to seed test products into DynamoDB Local
# Usage: ./seed-productos.sh

ENDPOINT_URL="http://localhost:8000"
TABLE_NAME="TablaProductos"

echo "🌱 Seeding products into $TABLE_NAME..."

# Product 1: Coca Cola 2L
aws dynamodb put-item \
  --table-name $TABLE_NAME \
  --item '{
    "id": {"S": "101"},
    "nombre": {"S": "Coca Cola 2L"},
    "codigo_barras": {"S": "7501234567890"},
    "precio": {"S": "28.50"}
  }' \
  --endpoint-url $ENDPOINT_URL

echo "✅ Product 101 - Coca Cola 2L inserted"

# Product 2: Pepsi 2L
aws dynamodb put-item \
  --table-name $TABLE_NAME \
  --item '{
    "id": {"S": "102"},
    "nombre": {"S": "Pepsi 2L"},
    "codigo_barras": {"S": "7501234567891"},
    "precio": {"S": "27.00"}
  }' \
  --endpoint-url $ENDPOINT_URL

echo "✅ Product 102 - Pepsi 2L inserted"

# Product 3: Leche Entera 1L
aws dynamodb put-item \
  --table-name $TABLE_NAME \
  --item '{
    "id": {"S": "103"},
    "nombre": {"S": "Leche Entera 1L"},
    "codigo_barras": {"S": "7501234567892"},
    "precio": {"S": "22.00"}
  }' \
  --endpoint-url $ENDPOINT_URL

echo "✅ Product 103 - Leche Entera 1L inserted"

# Product 4: Pan Blanco
aws dynamodb put-item \
  --table-name $TABLE_NAME \
  --item '{
    "id": {"S": "104"},
    "nombre": {"S": "Pan Blanco"},
    "codigo_barras": {"S": "7501234567893"},
    "precio": {"S": "15.50"}
  }' \
  --endpoint-url $ENDPOINT_URL

echo "✅ Product 104 - Pan Blanco inserted"

# Product 5: Arroz 1kg
aws dynamodb put-item \
  --table-name $TABLE_NAME \
  --item '{
    "id": {"S": "105"},
    "nombre": {"S": "Arroz 1kg"},
    "codigo_barras": {"S": "7501234567894"},
    "precio": {"S": "18.00"}
  }' \
  --endpoint-url $ENDPOINT_URL

echo "✅ Product 105 - Arroz 1kg inserted"

# Product 6: Aceite Vegetal 1L
aws dynamodb put-item \
  --table-name $TABLE_NAME \
  --item '{
    "id": {"S": "106"},
    "nombre": {"S": "Aceite Vegetal 1L"},
    "codigo_barras": {"S": "7501234567895"},
    "precio": {"S": "35.00"}
  }' \
  --endpoint-url $ENDPOINT_URL

echo "✅ Product 106 - Aceite Vegetal 1L inserted"

echo ""
echo "🎉 Successfully seeded 6 products into $TABLE_NAME"
echo ""
echo "Test queries:"
echo "  By barcode: curl 'http://localhost:3000/productos?q=7501234567890'"
echo "  By name:    curl 'http://localhost:3000/productos?q=Coca'"
