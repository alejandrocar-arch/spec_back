# Implementation Plan: POS Serverless Migration V1

## Overview

Este plan de implementación convierte el diseño de arquitectura serverless en tareas concretas de codificación. El enfoque es incremental: primero establecemos la estructura del proyecto y la infraestructura SAM, luego implementamos ProductosLambda (búsqueda de productos), seguido de VentasLambda (registro de ventas), y finalmente agregamos testing y documentación.

Cada tarea construye sobre las anteriores, asegurando que el código se integre progresivamente y que no queden componentes huérfanos. Las tareas de testing están marcadas como opcionales (*) para permitir un MVP más rápido si es necesario.

## Tasks

- [x] 1. Configurar estructura del proyecto y Maven multi-módulo
  - Crear directorio raíz `sales-api-serverless/`
  - Crear subdirectorios `productos-lambda/` y `ventas-lambda/`
  - Crear `pom.xml` raíz con configuración multi-módulo
  - Configurar `productos-lambda/pom.xml` con dependencias AWS Lambda y SDK v2
  - Configurar `ventas-lambda/pom.xml` con dependencias AWS Lambda y SDK v2
  - Configurar maven-shade-plugin en ambos módulos para crear uber JARs
  - Configurar Java 21 como source y target version
  - _Requirements: 7.1, 7.2, 7.3, 7.5, 7.6, 7.7, 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7_

- [x] 2. Crear SAM template con infraestructura base
  - Crear `template.yaml` en el directorio raíz
  - Definir API Gateway con CORS habilitado
  - Definir ProductosLambda function con runtime Java 21, 512MB memory, 30s timeout
  - Definir VentasLambda function con runtime Java 21, 512MB memory, 30s timeout
  - Configurar rutas: GET /productos → ProductosLambda, POST /ventas → VentasLambda
  - Definir TablaProductos con partition key "id" (String)
  - Definir GSI "CodigoBarrasIndex" en TablaProductos (partition key: codigo_barras)
  - Definir GSI "NombreIndex" en TablaProductos (partition key: nombre)
  - Definir TablaVentas con partition key "ventaId" (String)
  - Configurar billing mode on-demand para ambas tablas
  - Habilitar point-in-time recovery en ambas tablas
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 2.2, 3.1, 3.2, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 3. Configurar IAM roles y environment variables en SAM template
  - Definir IAM role para ProductosLambda con permisos dynamodb:Query y dynamodb:GetItem
  - Definir IAM role para VentasLambda con permisos dynamodb:PutItem
  - Configurar environment variable PRODUCTOS_TABLE_NAME para ProductosLambda usando !Ref
  - Configurar environment variable VENTAS_TABLE_NAME para VentasLambda usando !Ref
  - Configurar CloudWatch Logs retention period de 7 días
  - Agregar AWS managed policies para CloudWatch Logs access
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 17.1, 17.2, 17.3, 17.4, 17.5, 18.5, 18.6_

- [x] 4. Definir outputs en SAM template
  - Crear output "ProductosApiUrl" con URL completa del endpoint GET /productos
  - Crear output "VentasApiUrl" con URL completa del endpoint POST /ventas
  - Configurar outputs para ser visibles en console después de deployment
  - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6_

- [x] 5. Implementar DTOs y excepciones compartidas para ProductosLambda
  - Copiar y adaptar ProductDTO del proyecto Spring Boot existente
  - Crear ErrorResponse con campos: statusCode, message, timestamp
  - Crear ProductNotFoundException que extienda RuntimeException
  - Asegurar que ProductDTO use BigDecimal para precio
  - Configurar serialización JSON con Jackson para camelCase
  - _Requirements: 8.1, 8.3, 12.4, 16.1, 16.3, 16.5, 16.6_

- [x] 6. Implementar ProductRepository (DynamoDB access layer)
  - Crear clase ProductRepository en productos-lambda
  - Inyectar DynamoDbClient (AWS SDK v2) en constructor
  - Leer PRODUCTOS_TABLE_NAME desde environment variable usando System.getenv()
  - Implementar método queryByCodigoBarras usando Query operation con CodigoBarrasIndex GSI
  - Implementar método queryByNombre usando Query operation con NombreIndex GSI
  - Mapear DynamoDB items a ProductDTO (id String→Long, precio String→BigDecimal)
  - Lanzar ProductNotFoundException cuando no se encuentre producto
  - Manejar DynamoDbException y logear errores
  - _Requirements: 2.4, 2.5, 2.9, 2.10, 11.1, 11.2, 11.3, 11.6, 11.7, 17.4, 17.5_

- [x] 7. Implementar ProductSearchService (business logic)
  - Crear clase ProductSearchService en productos-lambda
  - Inyectar ProductRepository en constructor
  - Implementar método searchProduct(String query) que determina tipo de búsqueda
  - Implementar método privado isNumeric(String query) para detectar código de barras
  - Si query es numérico, llamar productRepository.queryByCodigoBarras()
  - Si query es alfabético, llamar productRepository.queryByNombre()
  - Propagar ProductNotFoundException al handler
  - _Requirements: 2.4, 2.5, 8.3, 10.1, 10.2, 10.5, 10.6, 10.7_

- [x] 8. Implementar ProductosHandler (Lambda entry point)
  - Crear clase ProductosHandler que implemente RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
  - Inicializar ProductSearchService y ObjectMapper en constructor
  - Implementar handleRequest method que extrae query parameter "q"
  - Validar presencia de query parameter, retornar 400 si falta
  - Llamar searchService.searchProduct(query)
  - Construir APIGatewayProxyResponseEvent con status 200 y ProductDTO en body
  - Capturar IllegalArgumentException → retornar 400 con error message
  - Capturar ProductNotFoundException → retornar 404 con error message
  - Capturar Exception genérica → retornar 500 con "Internal server error"
  - Logear request details (INFO), response details (INFO), y errores (ERROR)
  - Incluir correlation ID del API Gateway request context en logs
  - _Requirements: 2.3, 2.6, 2.7, 2.8, 9.1, 9.2, 9.5, 9.6, 9.7, 12.1, 12.2, 12.3, 12.5, 12.6, 16.1, 16.3, 16.5, 18.1, 18.2, 18.3, 18.4_

- [x]* 9. Escribir unit tests para ProductSearchService
  - Test: searchProduct con query numérico debe llamar queryByCodigoBarras
  - Test: searchProduct con query alfabético debe llamar queryByNombre
  - Test: searchProduct cuando repository retorna vacío debe lanzar ProductNotFoundException
  - Test: isNumeric debe retornar true para "7501234567890" y false para "Coca"
  - Usar Mockito para mockear ProductRepository
  - _Requirements: 10.7_

- [x]* 10. Escribir integration tests para ProductosHandler
  - Configurar DynamoDB Local en Docker para testing
  - Crear tabla TablaProductos con GSIs en DynamoDB Local
  - Seed test data: producto con id="101", codigo_barras="7501234567890", nombre="Coca Cola 2L"
  - Test: handleRequest con barcode válido debe retornar 200 con producto
  - Test: handleRequest con nombre válido debe retornar 200 con producto
  - Test: handleRequest con query inexistente debe retornar 404
  - Test: handleRequest sin query parameter debe retornar 400
  - Limpiar datos después de cada test
  - _Requirements: 2.6, 2.7, 2.8_

- [x] 11. Checkpoint - Validar ProductosLambda localmente
  - Ejecutar `mvn clean package` para compilar productos-lambda
  - Ejecutar `sam build` para preparar deployment package
  - Iniciar DynamoDB Local en puerto 8000
  - Seed test data en DynamoDB Local
  - Ejecutar `sam local start-api` para iniciar API Gateway local
  - Probar GET http://localhost:3000/productos?q=7501234567890 (esperar 200)
  - Probar GET http://localhost:3000/productos?q=Coca (esperar 200)
  - Probar GET http://localhost:3000/productos?q=99999 (esperar 404)
  - Probar GET http://localhost:3000/productos (esperar 400)
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 14.3, 14.4, 15.1, 15.3, 15.4, 15.5_

- [x] 12. Implementar DTOs y excepciones compartidas para VentasLambda
  - Copiar y adaptar CreateSaleRequest del proyecto Spring Boot existente
  - Extender CreateSaleRequest con campos: productoId (String), cantidad (Integer), total (BigDecimal)
  - Crear SaleDTO simplificado con campos: ventaId, productoId, cantidad, total, fecha
  - Crear ValidationException que extienda RuntimeException
  - Reutilizar ErrorResponse creado anteriormente
  - Configurar serialización JSON con Jackson para camelCase
  - _Requirements: 8.2, 8.4, 12.4, 16.2, 16.4, 16.5, 16.6_

- [x] 13. Implementar SaleRepository (DynamoDB access layer)
  - Crear clase SaleRepository en ventas-lambda
  - Inyectar DynamoDbClient (AWS SDK v2) en constructor
  - Leer VENTAS_TABLE_NAME desde environment variable usando System.getenv()
  - Implementar método saveSale(SaleDTO sale) usando PutItem operation
  - Mapear SaleDTO a DynamoDB item (total BigDecimal→String, fecha LocalDateTime→String ISO 8601)
  - Manejar DynamoDbException y logear errores
  - Retornar SaleDTO guardado
  - _Requirements: 3.4, 3.8, 3.9, 11.4, 11.5, 11.6, 11.7, 17.4, 17.5_

- [x] 14. Implementar SaleRegistrationService (business logic)
  - Crear clase SaleRegistrationService en ventas-lambda
  - Inyectar SaleRepository en constructor
  - Implementar método registerSale(CreateSaleRequest request)
  - Implementar método privado validateRequest que verifica terminalId y cashierId no sean blank
  - Lanzar ValidationException si validación falla
  - Generar ventaId único usando UUID.randomUUID().toString()
  - Generar fecha actual usando LocalDateTime.now(ZoneOffset.UTC)
  - Crear SaleDTO con datos validados, ventaId generado y fecha
  - Llamar saleRepository.saveSale(sale)
  - Retornar SaleDTO guardado
  - _Requirements: 3.4, 3.6, 3.10, 8.4, 10.3, 10.4, 10.5, 10.6, 10.7_

- [x] 15. Implementar VentasHandler (Lambda entry point)
  - Crear clase VentasHandler que implemente RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
  - Inicializar SaleRegistrationService y ObjectMapper en constructor
  - Implementar handleRequest method que parsea request body a CreateSaleRequest
  - Validar presencia de request body, retornar 400 si falta
  - Llamar registrationService.registerSale(request)
  - Construir APIGatewayProxyResponseEvent con status 201 y SaleDTO en body
  - Capturar JsonProcessingException → retornar 400 con "Invalid request body"
  - Capturar ValidationException → retornar 400 con validation error message
  - Capturar DynamoDbException → retornar 500 con "Failed to save sale"
  - Capturar Exception genérica → retornar 500 con "Internal server error"
  - Logear request details (INFO), response details (INFO), y errores (ERROR)
  - Incluir correlation ID del API Gateway request context en logs
  - _Requirements: 3.3, 3.5, 3.6, 3.7, 9.3, 9.4, 9.5, 9.6, 9.7, 12.1, 12.2, 12.3, 12.5, 12.6, 16.2, 16.4, 16.5, 18.1, 18.2, 18.3, 18.4_

- [x]* 16. Escribir unit tests para SaleRegistrationService
  - Test: registerSale con request válido debe generar ventaId en formato UUID
  - Test: registerSale con terminalId null debe lanzar ValidationException
  - Test: registerSale con cashierId blank debe lanzar ValidationException
  - Test: registerSale debe asignar fecha actual en UTC
  - Test: registerSale debe llamar saleRepository.saveSale con datos correctos
  - Usar Mockito para mockear SaleRepository
  - _Requirements: 10.7_

- [ ]* 17. Escribir integration tests para VentasHandler
  - Configurar DynamoDB Local en Docker para testing
  - Crear tabla TablaVentas en DynamoDB Local
  - Test: handleRequest con sale válido debe retornar 201 y guardar en DynamoDB
  - Test: handleRequest con JSON inválido debe retornar 400
  - Test: handleRequest con terminalId faltante debe retornar 400
  - Test: handleRequest sin body debe retornar 400
  - Verificar que sale se guardó correctamente en DynamoDB después de cada test
  - Limpiar datos después de cada test
  - _Requirements: 3.5, 3.6, 3.7_

- [x] 18. Checkpoint - Validar VentasLambda localmente
  - Ejecutar `mvn clean package` para compilar ventas-lambda
  - Ejecutar `sam build` para preparar deployment package
  - Asegurar que DynamoDB Local esté corriendo en puerto 8000
  - Ejecutar `sam local start-api` para iniciar API Gateway local
  - Probar POST http://localhost:3000/ventas con body válido (esperar 201)
  - Probar POST http://localhost:3000/ventas con body inválido (esperar 400)
  - Probar POST http://localhost:3000/ventas sin terminalId (esperar 400)
  - Verificar que sale se guardó en DynamoDB Local
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 14.3, 14.4, 15.2, 15.3, 15.4, 15.5_

- [x] 19. Crear README.md con documentación del proyecto
  - Documentar descripción general del proyecto y arquitectura
  - Documentar prerequisitos: Java 21, Maven 3.8+, SAM CLI 1.100+, Docker
  - Documentar comandos de build: `mvn clean package`, `sam build`
  - Documentar comandos de testing local: `sam local start-api`, curl examples
  - Documentar comandos de deployment: `sam deploy --guided`, `sam deploy`
  - Documentar cómo obtener URLs de endpoints después de deployment
  - Documentar estructura del proyecto y responsabilidades de cada módulo
  - Incluir ejemplos de requests y responses para ambos endpoints
  - Documentar cómo configurar DynamoDB Local para desarrollo
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 15.1, 15.2, 15.3, 15.4, 15.5_

- [x] 20. Crear scripts de seed data para testing local
  - Crear script `seed-productos.sh` que inserta productos de prueba en DynamoDB Local
  - Crear script `seed-ventas.sh` que inserta ventas de prueba en DynamoDB Local
  - Incluir al menos 5 productos con diferentes códigos de barras y nombres
  - Documentar cómo ejecutar scripts en README.md
  - Asegurar que scripts usen AWS CLI con endpoint-url http://localhost:8000
  - _Requirements: 15.3_

- [x] 21. Validar deployment completo en AWS
  - Ejecutar `sam deploy --guided` para primer deployment
  - Proporcionar stack name: sales-api-serverless
  - Confirmar cambios y permitir creación de IAM roles
  - Esperar a que deployment complete (CREATE_COMPLETE)
  - Capturar ProductosApiUrl y VentasApiUrl de outputs
  - Probar GET {ProductosApiUrl}?q=test (puede retornar 404 si no hay datos)
  - Probar POST {VentasApiUrl} con body válido
  - Verificar logs en CloudWatch Logs: /aws/lambda/ProductosLambda y /aws/lambda/VentasLambda
  - Verificar tablas creadas: `aws dynamodb list-tables`
  - Verificar funciones Lambda: `aws lambda list-functions`
  - _Requirements: 14.5, 14.6, 19.5, 19.6_

- [x] 22. Final checkpoint - Validación completa del sistema
  - Verificar que ambos endpoints respondan correctamente en AWS
  - Verificar que logs se generen correctamente en CloudWatch
  - Verificar que IAM roles tengan permisos correctos (least privilege)
  - Verificar que tablas DynamoDB tengan GSIs configurados
  - Verificar que billing mode sea on-demand
  - Verificar que point-in-time recovery esté habilitado
  - Documentar cualquier issue encontrado
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 2.2, 2.9, 2.10, 3.1, 3.2, 3.8, 3.9, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 18.5, 18.6_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints (tasks 11, 18, 22) ensure incremental validation and provide opportunities to ask questions
- Property-based testing is not applicable for this IaC/serverless project
- Unit tests and integration tests are complementary and focus on business logic validation
- SAM local testing provides end-to-end validation before AWS deployment
- All monetary values (precio, total) are stored as strings in DynamoDB to preserve precision
- Environment variables are injected via SAM template using !Ref intrinsic function
- IAM roles follow least privilege principle (read-only for ProductosLambda, write-only for VentasLambda)
