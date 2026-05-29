#!/bin/bash

# Script de validación pre-deployment
# Valida que todo esté listo para desplegar en AWS

set -e

echo "🔍 Validación Pre-Deployment - Sales API Serverless"
echo "=================================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

# Function to print success
success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to print error
error() {
    echo -e "${RED}❌ $1${NC}"
    ((ERRORS++))
}

# Function to print warning
warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
    ((WARNINGS++))
}

echo "1️⃣  Verificando herramientas requeridas..."
echo "-------------------------------------------"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        success "Java $JAVA_VERSION instalado"
    else
        error "Java 21+ requerido, encontrado: Java $JAVA_VERSION"
    fi
else
    error "Java no encontrado"
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    success "Maven $MVN_VERSION instalado"
else
    error "Maven no encontrado"
fi

# Check AWS CLI
if command -v aws &> /dev/null; then
    AWS_VERSION=$(aws --version | awk '{print $1}' | cut -d'/' -f2)
    success "AWS CLI $AWS_VERSION instalado"
else
    error "AWS CLI no encontrado"
fi

# Check SAM CLI
if command -v sam &> /dev/null; then
    SAM_VERSION=$(sam --version | awk '{print $4}')
    success "SAM CLI $SAM_VERSION instalado"
else
    error "SAM CLI no encontrado"
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
    success "Docker $DOCKER_VERSION instalado"
else
    warning "Docker no encontrado (necesario para testing local)"
fi

echo ""
echo "2️⃣  Verificando configuración AWS..."
echo "-------------------------------------------"

# Check AWS credentials
if aws sts get-caller-identity &> /dev/null; then
    AWS_ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
    AWS_USER=$(aws sts get-caller-identity --query Arn --output text | cut -d'/' -f2)
    success "AWS configurado - Account: $AWS_ACCOUNT, User: $AWS_USER"
else
    error "AWS CLI no configurado o credenciales inválidas"
fi

echo ""
echo "3️⃣  Validando estructura del proyecto..."
echo "-------------------------------------------"

# Check template.yaml
if [ -f "template.yaml" ]; then
    success "template.yaml encontrado"
else
    error "template.yaml no encontrado"
fi

# Check pom.xml files
if [ -f "pom.xml" ]; then
    success "pom.xml raíz encontrado"
else
    error "pom.xml raíz no encontrado"
fi

if [ -f "productos-lambda/pom.xml" ]; then
    success "productos-lambda/pom.xml encontrado"
else
    error "productos-lambda/pom.xml no encontrado"
fi

if [ -f "ventas-lambda/pom.xml" ]; then
    success "ventas-lambda/pom.xml encontrado"
else
    error "ventas-lambda/pom.xml no encontrado"
fi

# Check handler classes
if [ -f "productos-lambda/src/main/java/com/supermarket/sales/serverless/productos/handler/ProductosHandler.java" ]; then
    success "ProductosHandler.java encontrado"
else
    error "ProductosHandler.java no encontrado"
fi

if [ -f "ventas-lambda/src/main/java/com/supermarket/sales/serverless/ventas/handler/VentasHandler.java" ]; then
    success "VentasHandler.java encontrado"
else
    error "VentasHandler.java no encontrado"
fi

echo ""
echo "4️⃣  Validando template SAM..."
echo "-------------------------------------------"

# Validate SAM template
if sam validate --lint &> /dev/null; then
    success "Template SAM válido"
else
    error "Template SAM inválido"
    sam validate --lint
fi

echo ""
echo "5️⃣  Compilando proyecto Maven..."
echo "-------------------------------------------"

# Clean and compile
if mvn clean package -DskipTests &> /dev/null; then
    success "Compilación Maven exitosa"
    
    # Check JAR files
    if [ -f "productos-lambda/target/productos-lambda-1.0.0.jar" ]; then
        JAR_SIZE=$(du -h productos-lambda/target/productos-lambda-1.0.0.jar | cut -f1)
        success "productos-lambda JAR generado ($JAR_SIZE)"
    else
        error "productos-lambda JAR no generado"
    fi
    
    if [ -f "ventas-lambda/target/ventas-lambda-1.0.0.jar" ]; then
        JAR_SIZE=$(du -h ventas-lambda/target/ventas-lambda-1.0.0.jar | cut -f1)
        success "ventas-lambda JAR generado ($JAR_SIZE)"
    else
        error "ventas-lambda JAR no generado"
    fi
else
    error "Compilación Maven falló"
    mvn clean package -DskipTests
fi

echo ""
echo "6️⃣  Ejecutando SAM build..."
echo "-------------------------------------------"

# SAM build
if sam build &> /dev/null; then
    success "SAM build exitoso"
    
    # Check build artifacts
    if [ -d ".aws-sam/build/ProductosLambdaFunction" ]; then
        success "ProductosLambdaFunction build artifact encontrado"
    else
        error "ProductosLambdaFunction build artifact no encontrado"
    fi
    
    if [ -d ".aws-sam/build/VentasLambdaFunction" ]; then
        success "VentasLambdaFunction build artifact encontrado"
    else
        error "VentasLambdaFunction build artifact no encontrado"
    fi
else
    error "SAM build falló"
    sam build
fi

echo ""
echo "7️⃣  Verificando permisos IAM (opcional)..."
echo "-------------------------------------------"

# Check CloudFormation permissions
if aws cloudformation describe-stacks --stack-name test-permissions-check &> /dev/null; then
    success "Permisos CloudFormation OK"
elif aws cloudformation list-stacks &> /dev/null; then
    success "Permisos CloudFormation básicos OK"
else
    warning "No se pudieron verificar permisos CloudFormation"
fi

# Check Lambda permissions
if aws lambda list-functions --max-items 1 &> /dev/null; then
    success "Permisos Lambda OK"
else
    warning "No se pudieron verificar permisos Lambda"
fi

# Check DynamoDB permissions
if aws dynamodb list-tables --max-items 1 &> /dev/null; then
    success "Permisos DynamoDB OK"
else
    warning "No se pudieron verificar permisos DynamoDB"
fi

echo ""
echo "=================================================="
echo "📊 Resumen de Validación"
echo "=================================================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ Todas las validaciones pasaron exitosamente${NC}"
    echo ""
    echo "🚀 El proyecto está listo para desplegar en AWS"
    echo ""
    echo "Próximos pasos:"
    echo "  1. sam deploy --guided"
    echo "  2. Seguir el wizard de deployment"
    echo "  3. Probar los endpoints"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Validación completada con $WARNINGS advertencias${NC}"
    echo ""
    echo "El proyecto puede desplegarse, pero revisa las advertencias"
    exit 0
else
    echo -e "${RED}❌ Validación falló con $ERRORS errores y $WARNINGS advertencias${NC}"
    echo ""
    echo "Por favor corrige los errores antes de desplegar"
    exit 1
fi
