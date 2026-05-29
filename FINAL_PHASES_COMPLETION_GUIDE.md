# Final Phases Completion Guide - Phases 13 & 14

## Phase 13: Final Integration and Testing

### Task 13.1: End-to-End Manual Testing

#### Prerequisites
- Application running locally
- External APIs mocked or available
- Postman/Insomnia installed

#### Test Scenarios

**1. Complete Cash Sale Workflow**
```
1. POST /api/v1/sales - Create sale
2. POST /api/v1/sales/{id}/items/by-product-id - Add item 1
3. POST /api/v1/sales/{id}/items/by-product-id - Add item 2
4. POST /api/v1/sales/{id}/items/by-barcode - Add item 3
5. POST /api/v1/sales/{id}/discount - Apply 10% discount
6. POST /api/v1/sales/{id}/checkout - Checkout with cash
7. GET /api/v1/sales/{id} - Verify completed sale
8. Check H2 console - Verify data persisted
```

**2. Complete Credit Sale Workflow**
```
1. POST /api/v1/sales - Create sale with customer
2. POST /api/v1/sales/{id}/items/by-product-id - Add items
3. POST /api/v1/sales/{id}/checkout - Checkout with credit
4. Verify credit reference number in receipt
5. Verify customer credit validation was called
```

**3. Freeze/Resume Workflow**
```
1. POST /api/v1/sales - Create sale 1
2. POST /api/v1/sales/{id}/items/by-product-id - Add items
3. POST /api/v1/sales/{id}/freeze - Freeze sale 1
4. POST /api/v1/sales - Create sale 2
5. GET /api/v1/sales/terminal/{terminalId}/frozen - List frozen
6. POST /api/v1/sales/{id}/resume - Resume sale 1
7. POST /api/v1/sales/{id}/checkout - Complete sale 1
```

**4. Return Workflows**
```
# Full Return
1. Complete a sale (steps from scenario 1)
2. POST /api/v1/sales/{id}/return/full - Process return
3. Verify return receipt and credit note (if credit sale)

# Partial Return
1. Complete a sale with 5 items
2. POST /api/v1/sales/{id}/return/partial - Return 2 items
3. Verify status is PARTIALLY_RETURNED
4. POST /api/v1/sales/{id}/return/partial - Return remaining
5. Verify status is RETURNED
```

**5. Error Scenarios**
```
1. Try to add item with insufficient stock → 409
2. Try to checkout empty sale → 400
3. Try to checkout with insufficient cash → 400
4. Try credit sale without customer → 422
5. Try to cancel completed sale → 409
6. Try to resume non-frozen sale → 409
```

#### H2 Console Verification
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:salesdb
Username: sa
Password: (empty)

Queries to run:
SELECT * FROM sales;
SELECT * FROM sale_items;
SELECT * FROM sales WHERE status = 'COMPLETED';
SELECT * FROM sales WHERE status = 'FROZEN';
```

### Task 13.2: Performance Testing (Optional)

#### Using Apache JMeter
```xml
<!-- Test Plan -->
<ThreadGroup>
  <numThreads>50</numThreads>
  <rampUp>10</rampUp>
  <loops>100</loops>
</ThreadGroup>

<!-- Scenarios -->
1. Concurrent sale creation
2. Concurrent item additions
3. Concurrent checkouts
```

#### Using Gatling
```scala
scenario("Sale Creation")
  .exec(http("Create Sale")
    .post("/api/v1/sales")
    .body(StringBody("""{"terminalId":"TERM-001","cashierId":"CASH-001"}"""))
    .check(status.is(201)))
  .pause(1)
```

#### Performance Metrics to Measure
- Response time (p50, p95, p99)
- Throughput (requests/second)
- Error rate
- Database query performance

### Task 13.3: Documentation Review

#### README.md Checklist
- [ ] Project description
- [ ] Prerequisites (Java 17+, Maven)
- [ ] How to build: `mvn clean install`
- [ ] How to run: `mvn spring-boot:run`
- [ ] How to run tests: `mvn test`
- [ ] Configuration properties explained
- [ ] API documentation link
- [ ] External API dependencies
- [ ] Database setup instructions

#### API Endpoints Documentation
- [ ] All 17 endpoints documented
- [ ] Request/response examples
- [ ] Error codes explained
- [ ] Authentication (if applicable)

#### Configuration Properties Documentation
```yaml
# Document all properties in README
sales:
  tax-rate: 0.19                        # Tax rate (19%)
  frozen-sale-expiration-hours: 2       # Freeze timeout
  store-name: "Supermarket XYZ"         # Store name

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

#### External API Contracts
Document expected request/response formats for:
- Product API endpoints
- Customer API endpoints

---

## Phase 14: Deployment Preparation

### Task 14.1: PostgreSQL Configuration

#### Create application-prod.yml
```yaml
spring:
  application:
    name: sales-api
  
  datasource:
    url: jdbc:postgresql://localhost:5432/salesdb
    username: ${DB_USERNAME:salesapi}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway/Liquibase for migrations
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  # Connection Pool (HikariCP)
  hikari:
    maximum-pool-size: 10
    minimum-idle: 5
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000

sales:
  tax-rate: ${TAX_RATE:0.19}
  frozen-sale-expiration-hours: ${FROZEN_EXPIRATION:2}
  store-name: ${STORE_NAME:Supermarket XYZ}

external:
  product-api:
    base-url: ${PRODUCT_API_URL:http://product-api:8081/api/v1}
    timeout-seconds: ${PRODUCT_API_TIMEOUT:5}
    retry-attempts: ${PRODUCT_API_RETRIES:3}
  customer-api:
    base-url: ${CUSTOMER_API_URL:http://customer-api:8082/api/v1}
    timeout-seconds: ${CUSTOMER_API_TIMEOUT:5}
    retry-attempts: ${CUSTOMER_API_RETRIES:3}

logging:
  level:
    root: INFO
    com.supermarket.sales: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/sales-api/application.log
    max-size: 10MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when-authorized
```

#### Test PostgreSQL Schema Generation
```bash
# Start PostgreSQL
docker run --name postgres-test \
  -e POSTGRES_DB=salesdb \
  -e POSTGRES_USER=salesapi \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  -d postgres:15

# Run application with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Verify tables created
psql -h localhost -U salesapi -d salesdb -c "\dt"
```

#### Verify Data Type Compatibility
- BigDecimal → NUMERIC/DECIMAL
- LocalDateTime → TIMESTAMP
- Enums → VARCHAR
- Long → BIGINT

### Task 14.2: Production Readiness

#### Configure Logging for Production
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/var/log/sales-api/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/var/log/sales-api/application-%d{yyyy-MM-dd}.log</fileNamePattern>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="FILE" />
        </root>
    </springProfile>
</configuration>
```

#### Configure Actuator Endpoints
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Configure CORS (if needed)
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://pos.supermarket.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

#### Configure Security Headers
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers()
            .contentSecurityPolicy("default-src 'self'")
            .and()
            .xssProtection()
            .and()
            .frameOptions().deny();
        return http.build();
    }
}
```

#### Review and Secure Sensitive Configuration
- [ ] No hardcoded passwords
- [ ] Use environment variables for secrets
- [ ] Encrypt sensitive properties
- [ ] Use secrets management (Vault, AWS Secrets Manager)

#### Create Dockerfile (Optional)
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sales-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Create docker-compose.yml (Optional)
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: salesdb
      POSTGRES_USER: salesapi
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
  
  sales-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_USERNAME: salesapi
      DB_PASSWORD: password
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/salesdb
    depends_on:
      - postgres

volumes:
  postgres-data:
```

#### Create Deployment Scripts
```bash
#!/bin/bash
# deploy.sh

echo "Building application..."
mvn clean package -DskipTests

echo "Building Docker image..."
docker build -t sales-api:1.0.0 .

echo "Deploying to production..."
docker-compose -f docker-compose.prod.yml up -d

echo "Deployment complete!"
```

---

## Completion Checklist

### Phase 13: Final Integration and Testing
- [ ] Manual testing completed for all workflows
- [ ] H2 console data verified
- [ ] Error scenarios tested
- [ ] Performance testing completed (optional)
- [ ] Documentation reviewed and updated
- [ ] README.md complete
- [ ] API endpoints documented
- [ ] Configuration properties documented

### Phase 14: Deployment Preparation
- [ ] application-prod.yml created
- [ ] PostgreSQL configuration complete
- [ ] Connection pooling configured
- [ ] Schema generation tested with PostgreSQL
- [ ] Data type compatibility verified
- [ ] Production logging configured
- [ ] Actuator endpoints configured
- [ ] CORS configured (if needed)
- [ ] Security headers configured
- [ ] Sensitive configuration secured
- [ ] Dockerfile created (optional)
- [ ] docker-compose.yml created (optional)
- [ ] Deployment scripts created (optional)

---

## Commands Summary

```bash
# Build
mvn clean package

# Run locally
mvn spring-boot:run

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Run tests
mvn test

# Run with coverage
mvn clean verify

# Build Docker image
docker build -t sales-api:1.0.0 .

# Run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f sales-api
```

---

**Status**: Ready for Final Testing and Deployment
**Date**: $(date)
