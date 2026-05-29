# Requirements Document

## Introduction

Este documento especifica los requerimientos para crear una versión serverless simplificada del sistema POS de supermercado existente (desarrollado en Spring Boot) utilizando AWS SAM (Serverless Application Model). La versión serverless V1 se enfoca únicamente en dos funcionalidades core: búsqueda de productos y registro de ventas, utilizando AWS Lambda, API Gateway y DynamoDB.

El objetivo es crear una arquitectura serverless funcional y desplegable que demuestre la viabilidad de migrar componentes del sistema POS existente a una infraestructura sin servidor, manteniendo la lógica de negocio y reutilizando componentes cuando sea posible.

## Glossary

- **API_Gateway**: Servicio AWS que expone endpoints HTTP y los enruta a funciones Lambda
- **Lambda_Function**: Función serverless de AWS que ejecuta código en respuesta a eventos
- **DynamoDB**: Base de datos NoSQL serverless de AWS
- **SAM_Template**: Archivo template.yaml que define la infraestructura serverless usando AWS SAM
- **ProductosLambda**: Función Lambda que maneja búsquedas de productos
- **VentasLambda**: Función Lambda que maneja el registro de ventas
- **TablaProductos**: Tabla DynamoDB que almacena información de productos
- **TablaVentas**: Tabla DynamoDB que almacena información de ventas
- **GSI**: Global Secondary Index, índice secundario en DynamoDB para consultas alternativas
- **Query_Parameter**: Parámetro de consulta en URL (ejemplo: ?q=valor)
- **Request_Body**: Cuerpo JSON de una petición HTTP POST
- **Response_Body**: Cuerpo JSON de una respuesta HTTP
- **Partition_Key**: Clave primaria de partición en DynamoDB
- **IAM_Role**: Rol de AWS Identity and Access Management que define permisos
- **Environment_Variable**: Variable de configuración inyectada en tiempo de ejecución
- **Handler_Method**: Método Java que procesa eventos Lambda
- **DTO**: Data Transfer Object, objeto para transferir datos entre capas
- **Service_Layer**: Capa de lógica de negocio que contiene servicios reutilizables
- **DynamoDB_Access_Layer**: Capa que abstrae operaciones de lectura/escritura en DynamoDB
- **Maven_Module**: Módulo independiente en un proyecto Maven multi-módulo
- **Deployment_Package**: Archivo JAR que contiene el código Lambda y sus dependencias

## Requirements

### Requirement 1: API Gateway Configuration

**User Story:** Como desarrollador, quiero que API Gateway exponga dos endpoints HTTP, para que los clientes puedan buscar productos y registrar ventas.

#### Acceptance Criteria

1. THE SAM_Template SHALL define an API_Gateway resource with two HTTP endpoints
2. THE API_Gateway SHALL expose a GET endpoint at path "/productos"
3. THE API_Gateway SHALL expose a POST endpoint at path "/ventas"
4. THE API_Gateway SHALL route GET "/productos" requests to ProductosLambda
5. THE API_Gateway SHALL route POST "/ventas" requests to VentasLambda
6. THE API_Gateway SHALL return HTTP status codes according to Lambda execution results
7. THE API_Gateway SHALL enable CORS for cross-origin requests

### Requirement 2: ProductosLambda Function

**User Story:** Como usuario del sistema POS, quiero buscar productos mediante un parámetro de consulta, para que pueda encontrar productos por código de barras o nombre.

#### Acceptance Criteria

1. THE SAM_Template SHALL define a Lambda_Function named ProductosLambda
2. THE ProductosLambda SHALL use Java 21 runtime
3. THE ProductosLambda SHALL accept GET requests with Query_Parameter "q"
4. WHEN Query_Parameter "q" contains only numeric characters, THE ProductosLambda SHALL search TablaProductos by codigo_barras using the GSI
5. WHEN Query_Parameter "q" contains alphabetic characters, THE ProductosLambda SHALL search TablaProductos by nombre using the GSI
6. WHEN a product is found, THE ProductosLambda SHALL return HTTP 200 with product data in Response_Body
7. WHEN no product is found, THE ProductosLambda SHALL return HTTP 404 with an error message
8. WHEN Query_Parameter "q" is missing, THE ProductosLambda SHALL return HTTP 400 with a validation error message
9. THE ProductosLambda SHALL have read permissions to TablaProductos via IAM_Role
10. THE ProductosLambda SHALL receive TablaProductos name as Environment_Variable

### Requirement 3: VentasLambda Function

**User Story:** Como cajero, quiero registrar una venta mediante una petición POST, para que la transacción quede almacenada en el sistema.

#### Acceptance Criteria

1. THE SAM_Template SHALL define a Lambda_Function named VentasLambda
2. THE VentasLambda SHALL use Java 21 runtime
3. THE VentasLambda SHALL accept POST requests with JSON Request_Body
4. WHEN Request_Body contains valid sale data, THE VentasLambda SHALL store the sale in TablaVentas
5. WHEN a sale is successfully stored, THE VentasLambda SHALL return HTTP 201 with sale confirmation in Response_Body
6. WHEN Request_Body is invalid or missing required fields, THE VentasLambda SHALL return HTTP 400 with validation errors
7. WHEN DynamoDB write operation fails, THE VentasLambda SHALL return HTTP 500 with an error message
8. THE VentasLambda SHALL have write permissions to TablaVentas via IAM_Role
9. THE VentasLambda SHALL receive TablaVentas name as Environment_Variable
10. THE VentasLambda SHALL generate a unique ventaId for each sale using UUID or timestamp-based strategy

### Requirement 4: TablaProductos DynamoDB Table

**User Story:** Como sistema, necesito almacenar productos en DynamoDB, para que las búsquedas sean rápidas y escalables.

#### Acceptance Criteria

1. THE SAM_Template SHALL define a DynamoDB table named TablaProductos
2. THE TablaProductos SHALL use "id" as Partition_Key with type String
3. THE TablaProductos SHALL store attributes: id, nombre, codigo_barras, precio
4. THE TablaProductos SHALL define a GSI named "CodigoBarrasIndex" with codigo_barras as Partition_Key
5. THE TablaProductos SHALL define a GSI named "NombreIndex" with nombre as Partition_Key
6. THE TablaProductos SHALL use on-demand billing mode for automatic scaling
7. THE TablaProductos SHALL enable point-in-time recovery for data protection

### Requirement 5: TablaVentas DynamoDB Table

**User Story:** Como sistema, necesito almacenar ventas en DynamoDB, para que las transacciones persistan de forma durable.

#### Acceptance Criteria

1. THE SAM_Template SHALL define a DynamoDB table named TablaVentas
2. THE TablaVentas SHALL use "ventaId" as Partition_Key with type String
3. THE TablaVentas SHALL store attributes: ventaId, productoId, cantidad, total, fecha
4. THE TablaVentas SHALL use on-demand billing mode for automatic scaling
5. THE TablaVentas SHALL enable point-in-time recovery for data protection

### Requirement 6: IAM Permissions

**User Story:** Como administrador de seguridad, quiero que las Lambdas tengan permisos mínimos necesarios, para que se siga el principio de menor privilegio.

#### Acceptance Criteria

1. THE SAM_Template SHALL define an IAM_Role for ProductosLambda with read-only access to TablaProductos
2. THE SAM_Template SHALL define an IAM_Role for VentasLambda with write access to TablaVentas
3. THE IAM_Role for ProductosLambda SHALL include permissions: dynamodb:Query, dynamodb:GetItem
4. THE IAM_Role for VentasLambda SHALL include permissions: dynamodb:PutItem
5. THE IAM_Role SHALL NOT grant permissions to unrelated AWS services
6. THE SAM_Template SHALL use AWS managed policies when appropriate for CloudWatch Logs access

### Requirement 7: Project Structure

**User Story:** Como desarrollador, quiero una estructura de directorios clara, para que el proyecto sea fácil de navegar y mantener.

#### Acceptance Criteria

1. THE project SHALL create a root directory named "sales-api-serverless"
2. THE project SHALL create a subdirectory "productos-lambda" containing ProductosLambda source code
3. THE project SHALL create a subdirectory "ventas-lambda" containing VentasLambda source code
4. THE project SHALL place SAM_Template at root level as "template.yaml"
5. THE productos-lambda SHALL contain a pom.xml file for Maven build
6. THE ventas-lambda SHALL contain a pom.xml file for Maven build
7. THE project SHALL create a root pom.xml for multi-module Maven build coordination

### Requirement 8: Code Reusability

**User Story:** Como desarrollador, quiero reutilizar código del proyecto Spring Boot existente, para que no tenga que reescribir lógica ya probada.

#### Acceptance Criteria

1. THE ProductosLambda SHALL reuse ProductDTO from the existing Spring Boot project
2. THE VentasLambda SHALL reuse CreateSaleRequest and SaleDTO from the existing Spring Boot project
3. THE ProductosLambda SHALL reuse validation logic for query parameters
4. THE VentasLambda SHALL reuse validation logic for sale data
5. THE project SHALL NOT reuse Spring controllers, JPA repositories, or Hibernate entities
6. THE project SHALL create a DynamoDB_Access_Layer to replace JPA repositories
7. THE project SHALL create Handler_Method classes to replace Spring controllers

### Requirement 9: Lambda Handler Implementation

**User Story:** Como desarrollador, quiero que cada Lambda tenga un handler bien estructurado, para que el código sea mantenible y testeable.

#### Acceptance Criteria

1. THE ProductosLambda SHALL implement a Handler_Method that receives APIGatewayProxyRequestEvent
2. THE ProductosLambda SHALL return APIGatewayProxyResponseEvent with appropriate HTTP status
3. THE VentasLambda SHALL implement a Handler_Method that receives APIGatewayProxyRequestEvent
4. THE VentasLambda SHALL return APIGatewayProxyResponseEvent with appropriate HTTP status
5. THE Handler_Method SHALL delegate business logic to Service_Layer classes
6. THE Handler_Method SHALL handle exceptions and convert them to appropriate HTTP responses
7. THE Handler_Method SHALL log incoming requests and outgoing responses for observability

### Requirement 10: Service Layer Implementation

**User Story:** Como desarrollador, quiero separar la lógica de negocio en servicios, para que el código sea reutilizable y testeable.

#### Acceptance Criteria

1. THE ProductosLambda SHALL include a ProductSearchService class
2. THE ProductSearchService SHALL implement search logic for codigo_barras and nombre
3. THE VentasLambda SHALL include a SaleRegistrationService class
4. THE SaleRegistrationService SHALL implement sale validation and storage logic
5. THE Service_Layer SHALL use DynamoDB_Access_Layer for data operations
6. THE Service_Layer SHALL NOT contain AWS Lambda-specific code
7. THE Service_Layer SHALL be unit testable without AWS infrastructure

### Requirement 11: DynamoDB Access Layer

**User Story:** Como desarrollador, quiero una capa de acceso a DynamoDB, para que las operaciones de base de datos estén abstraídas y sean fáciles de testear.

#### Acceptance Criteria

1. THE ProductosLambda SHALL include a ProductRepository class using AWS SDK v2
2. THE ProductRepository SHALL implement queryByCodigoBarras method using GSI
3. THE ProductRepository SHALL implement queryByNombre method using GSI
4. THE VentasLambda SHALL include a SaleRepository class using AWS SDK v2
5. THE SaleRepository SHALL implement saveSale method using PutItem operation
6. THE DynamoDB_Access_Layer SHALL use DynamoDbClient from AWS SDK v2
7. THE DynamoDB_Access_Layer SHALL read table names from Environment_Variable

### Requirement 12: Error Handling

**User Story:** Como usuario de la API, quiero recibir mensajes de error claros, para que pueda entender qué salió mal y cómo corregirlo.

#### Acceptance Criteria

1. WHEN a validation error occurs, THE Lambda_Function SHALL return HTTP 400 with error details in Response_Body
2. WHEN a resource is not found, THE Lambda_Function SHALL return HTTP 404 with a descriptive message
3. WHEN an internal error occurs, THE Lambda_Function SHALL return HTTP 500 with a generic error message
4. THE Response_Body for errors SHALL include fields: statusCode, message, timestamp
5. THE Lambda_Function SHALL log detailed error information to CloudWatch Logs
6. THE Lambda_Function SHALL NOT expose sensitive information in error responses

### Requirement 13: Maven Build Configuration

**User Story:** Como desarrollador, quiero que el proyecto compile con Maven, para que pueda generar los Deployment_Package necesarios.

#### Acceptance Criteria

1. THE productos-lambda pom.xml SHALL configure maven-shade-plugin to create an uber JAR
2. THE ventas-lambda pom.xml SHALL configure maven-shade-plugin to create an uber JAR
3. THE pom.xml SHALL declare AWS Lambda Java Core dependency version 1.2.3 or higher
4. THE pom.xml SHALL declare AWS SDK v2 DynamoDB dependency version 2.20.0 or higher
5. THE pom.xml SHALL declare AWS Lambda Java Events dependency for API Gateway integration
6. THE pom.xml SHALL configure Java 21 as source and target version
7. THE maven-shade-plugin SHALL exclude AWS SDK classes that are provided by Lambda runtime

### Requirement 14: SAM Build and Deploy

**User Story:** Como desarrollador, quiero usar comandos SAM estándar, para que pueda construir y desplegar la aplicación fácilmente.

#### Acceptance Criteria

1. WHEN "sam build" is executed, THE SAM_Template SHALL compile both Lambda functions
2. WHEN "sam build" completes, THE system SHALL create Deployment_Package for each Lambda
3. WHEN "sam local start-api" is executed, THE system SHALL start a local API Gateway emulator
4. WHEN "sam local start-api" is running, THE system SHALL accept HTTP requests to /productos and /ventas
5. WHEN "sam deploy --guided" is executed, THE system SHALL prompt for deployment parameters
6. WHEN "sam deploy" completes, THE system SHALL output API Gateway endpoint URLs
7. THE SAM_Template SHALL be compatible with SAM CLI version 1.100.0 or higher

### Requirement 15: Local Development Support

**User Story:** Como desarrollador, quiero probar las Lambdas localmente, para que pueda desarrollar sin desplegar a AWS constantemente.

#### Acceptance Criteria

1. WHEN "sam local start-api" is running, THE ProductosLambda SHALL respond to GET /productos?q=test
2. WHEN "sam local start-api" is running, THE VentasLambda SHALL respond to POST /ventas with test data
3. THE local environment SHALL use DynamoDB Local or mock DynamoDB for testing
4. THE local environment SHALL read Environment_Variable from SAM_Template
5. THE local environment SHALL log Lambda execution output to console
6. THE local environment SHALL support hot reload when code changes are detected

### Requirement 16: Response Format

**User Story:** Como cliente de la API, quiero recibir respuestas en formato JSON consistente, para que pueda parsear las respuestas fácilmente.

#### Acceptance Criteria

1. THE ProductosLambda SHALL return Response_Body with Content-Type "application/json"
2. THE VentasLambda SHALL return Response_Body with Content-Type "application/json"
3. WHEN ProductosLambda finds a product, THE Response_Body SHALL include fields: id, nombre, codigo_barras, precio
4. WHEN VentasLambda creates a sale, THE Response_Body SHALL include fields: ventaId, productoId, cantidad, total, fecha
5. THE Response_Body SHALL use camelCase for JSON field names
6. THE Response_Body SHALL serialize BigDecimal values as strings to preserve precision

### Requirement 17: Environment Variables Configuration

**User Story:** Como operador, quiero configurar las Lambdas mediante variables de entorno, para que pueda cambiar configuraciones sin recompilar código.

#### Acceptance Criteria

1. THE SAM_Template SHALL define Environment_Variable "PRODUCTOS_TABLE_NAME" for ProductosLambda
2. THE SAM_Template SHALL define Environment_Variable "VENTAS_TABLE_NAME" for VentasLambda
3. THE SAM_Template SHALL use intrinsic function !Ref to inject table names dynamically
4. THE Lambda_Function SHALL read Environment_Variable using System.getenv() in Java
5. WHEN Environment_Variable is missing, THE Lambda_Function SHALL fail fast with a clear error message

### Requirement 18: Logging and Observability

**User Story:** Como operador, quiero que las Lambdas generen logs estructurados, para que pueda monitorear y depurar problemas en producción.

#### Acceptance Criteria

1. THE Lambda_Function SHALL log request details at INFO level when processing starts
2. THE Lambda_Function SHALL log response details at INFO level when processing completes
3. THE Lambda_Function SHALL log error details at ERROR level when exceptions occur
4. THE logs SHALL include correlation identifiers from API Gateway request context
5. THE logs SHALL be sent to CloudWatch Logs automatically by Lambda runtime
6. THE SAM_Template SHALL configure log retention period of 7 days for cost optimization

### Requirement 19: Deployment Outputs

**User Story:** Como desarrollador, quiero que SAM deploy muestre las URLs de los endpoints, para que pueda probar la API inmediatamente después del despliegue.

#### Acceptance Criteria

1. THE SAM_Template SHALL define an Output named "ProductosApiUrl"
2. THE SAM_Template SHALL define an Output named "VentasApiUrl"
3. THE ProductosApiUrl SHALL contain the full URL for GET /productos endpoint
4. THE VentasApiUrl SHALL contain the full URL for POST /ventas endpoint
5. WHEN "sam deploy" completes, THE system SHALL display both URLs in the console
6. THE Outputs SHALL be retrievable using "aws cloudformation describe-stacks" command

### Requirement 20: Simplicity and V1 Scope

**User Story:** Como product owner, quiero que la V1 sea simple y funcional, para que podamos validar la arquitectura serverless rápidamente.

#### Acceptance Criteria

1. THE project SHALL NOT implement authentication or authorization mechanisms
2. THE project SHALL NOT include a frontend application
3. THE project SHALL NOT implement retry logic or circuit breakers
4. THE project SHALL NOT integrate with external Product API or Customer API
5. THE project SHALL NOT implement scheduled tasks or background jobs
6. THE project SHALL focus exclusively on product search and sale registration
7. THE project SHALL prioritize functional deployment over advanced features
