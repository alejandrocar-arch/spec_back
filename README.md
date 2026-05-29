# 🛒 Sales API - Sistema POS para Supermercado

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage](https://img.shields.io/badge/Coverage-80%25+-success.svg)](https://github.com/jacoco/jacoco)

API REST para la gestión de transacciones de ventas en un sistema de Punto de Venta (POS) de supermercado. Maneja el ciclo completo de ventas incluyendo búsqueda de productos y clientes, creación de ventas, gestión de items, múltiples tipos de pago, procesamiento de checkout, congelamiento de ventas, cancelaciones y devoluciones (completas y parciales).

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Testing](#-testing)
- [Documentación API](#-documentación-api)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Endpoints Principales](#-endpoints-principales)
- [Modelo de Datos](#-modelo-de-datos)
- [Despliegue](#-despliegue)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## ✨ Características

### Gestión de Productos
- 🔍 Búsqueda de productos por nombre (coincidencia parcial)
- 📊 Búsqueda de productos por código de barras
- 📦 Validación de disponibilidad de stock
- 🔄 Actualización automática de inventario

### Gestión de Clientes
- 👤 Búsqueda de clientes por nombre
- 🆔 Búsqueda de clientes por número de documento
- 💳 Validación de estado de crédito para ventas a crédito

### Gestión de Ventas
- ➕ Creación de nuevas ventas
- 🛍️ Agregar items por ID de producto o código de barras
- ✏️ Actualizar cantidades de items
- 🗑️ Eliminar items de la venta
- 💰 Aplicar descuentos (porcentaje o monto fijo)
- 💵 Checkout con pago en efectivo (con cálculo de cambio)
- 💳 Checkout con pago a crédito (con validación de crédito)
- ❄️ Congelar ventas temporalmente
- ▶️ Reanudar ventas congeladas
- ❌ Cancelar ventas con razón
- 🔄 Devoluciones completas y parciales
- 📄 Generación de recibos y notas de crédito

### Características Técnicas
- 🔐 Validación de entrada en todos los endpoints
- 🚨 Manejo comprehensivo de excepciones
- 📊 Cálculos monetarios precisos con BigDecimal
- 🔄 Gestión de transacciones con @Transactional
- 🔁 Lógica de reintentos para APIs externas
- ⚡ Circuit breaker para resiliencia
- ⏰ Cancelación automática de ventas congeladas expiradas
- 📝 Logging estructurado
- 📖 Documentación OpenAPI/Swagger

---

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura en capas** (Layered Architecture) con separación clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────────┐
│                     REST Controllers                        │
│              (Capa de Presentación)                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│              (Lógica de Negocio)                            │
│  • SaleService                                              │
│  • ProductService                                           │
│  • CustomerService                                          │
│  • CalculationService                                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                           │
│              (Acceso a Datos)                               │
│  • SaleRepository                                           │
│  • SaleItemRepository                                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │   Database   │
                  │  H2/PostgreSQL│
                  └──────────────┘
```

### Integraciones Externas

```
┌──────────────┐         ┌──────────────┐
│ Product API  │◄────────┤ProductClient │
└──────────────┘         └──────────────┘
                                │
                                │
┌──────────────┐         ┌──────────────┐
│Customer API  │◄────────┤CustomerClient│
└──────────────┘         └──────────────┘
```

---

## 🛠️ Tecnologías Utilizadas

### Backend Framework
- **Spring Boot 3.2.0** - Framework principal
- **Spring Web** - REST API
- **Spring Data JPA** - Persistencia de datos
- **Spring Validation** - Validación de entrada
- **Spring Retry** - Lógica de reintentos
- **Spring Scheduling** - Tareas programadas

### Base de Datos
- **H2 Database** - Base de datos en memoria (desarrollo/testing)
- **PostgreSQL** - Base de datos relacional (producción)
- **Hibernate** - ORM (Object-Relational Mapping)

### Resiliencia
- **Resilience4j** - Circuit breaker y patrones de resiliencia
- **Apache HttpClient 5** - Connection pooling para APIs externas

### Documentación
- **SpringDoc OpenAPI 3** - Generación de documentación OpenAPI
- **Swagger UI** - Interfaz interactiva de documentación

### Testing
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking para unit tests
- **WireMock** - Mocking de APIs externas
- **Spring Boot Test** - Testing de integración
- **JaCoCo** - Cobertura de código

### Build & Deployment
- **Maven** - Gestión de dependencias y build
- **Java 17** - Versión de Java
- **Docker** (opcional) - Containerización

### Herramientas de Desarrollo
- **Lombok** (opcional) - Reducción de código boilerplate
- **SLF4J + Logback** - Logging
- **Jackson** - Serialización/deserialización JSON

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ☕ **Java 17 o superior**
  ```bash
  java -version
  ```

- 📦 **Maven 3.6+**
  ```bash
  mvn -version
  ```

- 🐘 **PostgreSQL 12+** (para producción)
  ```bash
  psql --version
  ```

- 🐳 **Docker** (opcional, para containerización)
  ```bash
  docker --version
  ```

---

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/sales-api.git
cd sales-api
```

### 2. Compilar el Proyecto

```bash
mvn clean install
```

Este comando:
- Descarga todas las dependencias
- Compila el código fuente
- Ejecuta todos los tests
- Genera el archivo JAR ejecutable

### 3. Verificar la Compilación

```bash
# El JAR se genera en:
ls -la target/sales-api-1.0.0.jar
```

---

## ⚙️ Configuración

### Configuración de Desarrollo (H2)

El archivo `src/main/resources/application.yml` contiene la configuración por defecto:

```yaml
spring:
  application:
    name: sales-api
  
  datasource:
    url: jdbc:h2:mem:salesdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

sales:
  tax-rate: 0.19                        # 19% IVA
  frozen-sale-expiration-hours: 2       # 2 horas
  store-name: "Supermarket XYZ"

external:
  product-api:
    base-url: http://localhost:8081/api/v1
    timeout-seconds: 5
    retry-attempts: 3
  customer-api:
    base-url: http://localhost:8082/api/v1
    timeout-seconds: 5
    retry-attempts: 3
```

### Configuración de Producción (PostgreSQL)

Crear `src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/salesdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

sales:
  tax-rate: ${TAX_RATE:0.19}
  frozen-sale-expiration-hours: ${FROZEN_EXPIRATION:2}
  store-name: ${STORE_NAME:Supermarket XYZ}

external:
  product-api:
    base-url: ${PRODUCT_API_URL}
    timeout-seconds: ${PRODUCT_API_TIMEOUT:5}
    retry-attempts: ${PRODUCT_API_RETRIES:3}
  customer-api:
    base-url: ${CUSTOMER_API_URL}
    timeout-seconds: ${CUSTOMER_API_TIMEOUT:5}
    retry-attempts: ${CUSTOMER_API_RETRIES:3}
```

### Variables de Entorno

```bash
# Base de datos
export DB_USERNAME=salesapi
export DB_PASSWORD=your_secure_password

# APIs externas
export PRODUCT_API_URL=https://api.products.com/v1
export CUSTOMER_API_URL=https://api.customers.com/v1

# Configuración de negocio
export TAX_RATE=0.19
export STORE_NAME="Mi Supermercado"
```

---

## ▶️ Ejecución

### Modo Desarrollo (H2)

```bash
# Opción 1: Con Maven
mvn spring-boot:run

# Opción 2: Con Java
java -jar target/sales-api-1.0.0.jar

# Opción 3: Con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en: **http://localhost:8080**

### Modo Producción (PostgreSQL)

```bash
# Con perfil de producción
java -jar target/sales-api-1.0.0.jar --spring.profiles.active=prod

# O con variables de entorno
SPRING_PROFILES_ACTIVE=prod java -jar target/sales-api-1.0.0.jar
```

### Consola H2 (Solo Desarrollo)

Acceder a: **http://localhost:8080/h2-console**

```
JDBC URL: jdbc:h2:mem:salesdb
Username: sa
Password: (dejar vacío)
```

### Docker (Opcional)

```bash
# Build imagen
docker build -t sales-api:1.0.0 .

# Run contenedor
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_USERNAME=salesapi \
  -e DB_PASSWORD=password \
  sales-api:1.0.0
```

---

## 🧪 Testing

### Ejecutar Todos los Tests

```bash
mvn test
```

### Ejecutar Tests Específicos

```bash
# Solo unit tests
mvn test -Dtest="*Test"

# Solo integration tests
mvn test -Dtest="*IntegrationTest"

# Test específico
mvn test -Dtest="SaleServiceImplTest"
```

### Cobertura de Código

```bash
# Generar reporte de cobertura
mvn clean verify

# Ver reporte HTML
open target/site/jacoco/index.html
```

**Objetivos de Cobertura:**
- ✅ Cobertura general: **≥ 80%**
- ✅ Capa de servicio: **≥ 90%**

### Tipos de Tests

#### Unit Tests (100+ tests)
- ✅ CalculationServiceTest - Cálculos monetarios
- ✅ ProductServiceTest - Integración con Product API
- ✅ CustomerServiceTest - Integración con Customer API
- ✅ SaleServiceTest - Lógica de negocio de ventas
- ✅ EntityTests - Lógica de entidades

#### Integration Tests (30+ tests)
- ✅ CashSaleFlowIntegrationTest - Flujo completo de venta en efectivo
- ✅ CreditSaleFlowIntegrationTest - Flujo completo de venta a crédito
- ✅ FreezeResumeFlowIntegrationTest - Congelamiento y reanudación
- ✅ FullReturnFlowIntegrationTest - Devolución completa
- ✅ PartialReturnFlowIntegrationTest - Devolución parcial
- ✅ ErrorScenariosIntegrationTest - Escenarios de error

---

## 📖 Documentación API

### OpenAPI Specification

Una vez que la aplicación esté ejecutándose, la documentación está disponible en:

**OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```

**Swagger UI (Interfaz Interactiva):**
```
http://localhost:8080/swagger-ui.html
```

### Características de la Documentación

- ✅ Todos los endpoints documentados
- ✅ Esquemas de request/response
- ✅ Códigos de estado HTTP
- ✅ Ejemplos de uso
- ✅ Interfaz "Try it out" para probar endpoints
- ✅ Modelos de datos con validaciones

---

## 📁 Estructura del Proyecto

```
sales-api/
├── src/
│   ├── main/
│   │   ├── java/com/supermarket/sales/
│   │   │   ├── SalesApiApplication.java          # Clase principal
│   │   │   ├── client/                           # Clientes API externos
│   │   │   │   ├── ProductApiClient.java
│   │   │   │   ├── ProductApiClientImpl.java
│   │   │   │   ├── CustomerApiClient.java
│   │   │   │   └── CustomerApiClientImpl.java
│   │   │   ├── config/                           # Configuraciones
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── RestTemplateConfig.java
│   │   │   ├── controller/                       # Controladores REST
│   │   │   │   ├── ProductSearchController.java
│   │   │   │   ├── CustomerSearchController.java
│   │   │   │   └── SaleController.java
│   │   │   ├── dto/                              # Data Transfer Objects
│   │   │   │   ├── error/
│   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   └── ValidationError.java
│   │   │   │   ├── external/
│   │   │   │   │   ├── ProductDTO.java
│   │   │   │   │   └── CustomerDTO.java
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateSaleRequest.java
│   │   │   │   │   ├── AddItemByProductIdRequest.java
│   │   │   │   │   ├── CheckoutRequest.java
│   │   │   │   │   └── ...
│   │   │   │   └── response/
│   │   │   │       ├── SaleDTO.java
│   │   │   │       ├── ReceiptDTO.java
│   │   │   │       ├── CheckoutResponse.java
│   │   │   │       └── ...
│   │   │   ├── entity/                           # Entidades JPA
│   │   │   │   ├── Sale.java
│   │   │   │   └── SaleItem.java
│   │   │   ├── enums/                            # Enumeraciones
│   │   │   │   ├── SaleStatus.java
│   │   │   │   ├── PaymentType.java
│   │   │   │   ├── DiscountType.java
│   │   │   │   └── CreditStatus.java
│   │   │   ├── exception/                        # Excepciones personalizadas
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ...
│   │   │   ├── mapper/                           # Mappers Entity-DTO
│   │   │   │   ├── SaleMapper.java
│   │   │   │   └── SaleItemMapper.java
│   │   │   ├── repository/                       # Repositorios JPA
│   │   │   │   ├── SaleRepository.java
│   │   │   │   └── SaleItemRepository.java
│   │   │   ├── scheduler/                        # Tareas programadas
│   │   │   │   └── FrozenSaleExpirationScheduler.java
│   │   │   └── service/                          # Servicios de negocio
│   │   │       ├── CalculationService.java
│   │   │       ├── CalculationServiceImpl.java
│   │   │       ├── ProductService.java
│   │   │       ├── ProductServiceImpl.java
│   │   │       ├── CustomerService.java
│   │   │       ├── CustomerServiceImpl.java
│   │   │       ├── SaleService.java
│   │   │       └── SaleServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml                   # Configuración principal
│   │       └── application-test.yml              # Configuración de tests
│   └── test/
│       ├── java/com/supermarket/sales/
│       │   ├── entity/                           # Tests de entidades
│       │   ├── integration/                      # Tests de integración
│       │   │   ├── BaseIntegrationTest.java
│       │   │   ├── CashSaleFlowIntegrationTest.java
│       │   │   └── ...
│       │   ├── repository/                       # Tests de repositorios
│       │   ├── scheduler/                        # Tests de schedulers
│       │   └── service/                          # Tests de servicios
│       └── resources/
│           └── application-test.yml
├── .kiro/                                        # Especificaciones del proyecto
│   └── specs/pos-api/
│       ├── requirements.md
│       ├── design.md
│       └── tasks.md
├── pom.xml                                       # Configuración Maven
├── Dockerfile                                    # Imagen Docker (opcional)
├── docker-compose.yml                            # Compose (opcional)
└── README.md                                     # Este archivo
```

---

## 🔌 Endpoints Principales

### Búsqueda de Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/products/search/by-name?name={name}` | Buscar productos por nombre |
| GET | `/api/v1/products/search/by-barcode?barcode={code}` | Buscar producto por código de barras |

### Búsqueda de Clientes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/customers/search/by-name?name={name}` | Buscar clientes por nombre |
| GET | `/api/v1/customers/search/by-document?documentNumber={doc}` | Buscar cliente por documento |

### Gestión de Ventas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/sales` | Crear nueva venta |
| GET | `/api/v1/sales/{saleId}` | Obtener venta por ID |
| GET | `/api/v1/sales/terminal/{terminalId}` | Listar ventas por terminal |
| GET | `/api/v1/sales/terminal/{terminalId}/frozen` | Listar ventas congeladas |

### Gestión de Items

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/sales/{saleId}/items/by-product-id` | Agregar item por ID de producto |
| POST | `/api/v1/sales/{saleId}/items/by-barcode` | Agregar item por código de barras |
| PUT | `/api/v1/sales/{saleId}/items/{itemId}/quantity` | Actualizar cantidad |
| DELETE | `/api/v1/sales/{saleId}/items/{itemId}` | Eliminar item |

### Operaciones de Venta

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/sales/{saleId}/discount` | Aplicar descuento |
| POST | `/api/v1/sales/{saleId}/checkout` | Procesar checkout |
| POST | `/api/v1/sales/{saleId}/freeze` | Congelar venta |
| POST | `/api/v1/sales/{saleId}/resume` | Reanudar venta |
| POST | `/api/v1/sales/{saleId}/cancel` | Cancelar venta |

### Devoluciones

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/sales/{saleId}/return/full` | Devolución completa |
| POST | `/api/v1/sales/{saleId}/return/partial` | Devolución parcial |

---

## 💾 Modelo de Datos

### Entidad: Sale

```java
@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String terminalId;
    private String cashierId;
    private Long customerId;
    
    @Enumerated(EnumType.STRING)
    private SaleStatus status;  // ACTIVE, FROZEN, COMPLETED, CANCELLED, RETURNED
    
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;  // CASH, CREDIT
    
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items;
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    // ... más campos
}
```

### Entidad: SaleItem

```java
@Entity
@Table(name = "sale_items")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;
    
    private Long productId;
    private String productName;
    private String barcode;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private Integer returnedQuantity;
}
```

### Diagrama ER

```
┌─────────────────────────────────────────┐
│                 SALE                    │
├─────────────────────────────────────────┤
│ PK  id                                  │
│     terminalId                          │
│     cashierId                           │
│     customerId                          │
│     status (ENUM)                       │
│     subtotal                            │
│     tax                                 │
│     discount                            │
│     total                               │
│     paymentType (ENUM)                  │
│     transactionId                       │
│     createdAt                           │
│     completedAt                         │
└──────────────┬──────────────────────────┘
               │ 1
               │
               │ *
┌──────────────┴──────────────────────────┐
│              SALE_ITEM                  │
├─────────────────────────────────────────┤
│ PK  id                                  │
│ FK  saleId                              │
│     productId                           │
│     productName                         │
│     barcode                             │
│     unitPrice                           │
│     quantity                            │
│     lineTotal                           │
│     returnedQuantity                    │
└─────────────────────────────────────────┘
```

---

## 🚢 Despliegue

### Despliegue Tradicional

```bash
# 1. Compilar
mvn clean package -DskipTests

# 2. Copiar JAR al servidor
scp target/sales-api-1.0.0.jar user@server:/opt/sales-api/

# 3. Ejecutar en servidor
ssh user@server
cd /opt/sales-api
nohup java -jar sales-api-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### Despliegue con Docker

```bash
# 1. Build imagen
docker build -t sales-api:1.0.0 .

# 2. Push a registry
docker tag sales-api:1.0.0 myregistry/sales-api:1.0.0
docker push myregistry/sales-api:1.0.0

# 3. Deploy
docker pull myregistry/sales-api:1.0.0
docker run -d \
  --name sales-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_USERNAME=salesapi \
  -e DB_PASSWORD=secret \
  myregistry/sales-api:1.0.0
```

### Despliegue con Docker Compose

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f sales-api

# Detener servicios
docker-compose down
```

### Despliegue en AWS (Serverless con SAM)

Ver documentación detallada en: [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)

```bash
# 1. Build
sam build

# 2. Deploy
sam deploy --guided

# 3. Obtener URLs
sam list endpoints
```

---

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor sigue estos pasos:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Estándares de Código

- ✅ Seguir convenciones de Java
- ✅ Escribir tests para nuevo código
- ✅ Mantener cobertura ≥ 80%
- ✅ Documentar métodos públicos con JavaDoc
- ✅ Usar nombres descriptivos para variables y métodos

---

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia Apache 2.0 - ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 👥 Autores

- **Tu Nombre** - *Desarrollo Inicial* - [tu-usuario](https://github.com/tu-usuario)

---

## 🙏 Agradecimientos

- Spring Boot Team por el excelente framework
- Comunidad de código abierto
- Todos los contribuidores

---

## 📞 Soporte

Para soporte, envía un email a support@supermarket.com o abre un issue en GitHub.

---

## 🔗 Enlaces Útiles

- [Documentación de Spring Boot](https://spring.io/projects/spring-boot)
- [Documentación de Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [OpenAPI Specification](https://swagger.io/specification/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

**Hecho con ❤️ usando Spring Boot**
