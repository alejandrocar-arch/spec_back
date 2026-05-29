#!/bin/bash

# Script to create DynamoDB tables locally
# Usage: ./create-tables-local.sh

ENDPOINT_URL="http://localhost:8000"

echo "📦 Creating DynamoDB tables locally..."
echo ""

# Create TablaProductos
echo "Creating TablaProductos..."
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
  --endpoint-url $ENDPOINT_URL \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  echo "✅ TablaProductos created successfully"
else
  echo "⚠️  TablaProductos might already exist or creation failed"
fi

echo ""

# Create TablaVentas
echo "Creating TablaVentas..."
aws dynamodb create-table \
  --table-name TablaVentas \
  --attribute-definitions AttributeName=ventaId,AttributeType=S \
  --key-schema AttributeName=ventaId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url $ENDPOINT_URL \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  echo "✅ TablaVentas created successfully"
else
  echo "⚠️  TablaVentas might already exist or creation failed"
fi

echo ""
echo "🎉 Table creation complete!"
echo ""
echo "Next steps:"
echo "  1. Run: ./scripts/seed-productos.sh"
echo "  2. Start API: sam local start-api --docker-network host"
