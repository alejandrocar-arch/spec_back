package com.supermarket.sales.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for Sales API documentation.
 * Configures Swagger UI and API documentation endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salesApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sales API")
                        .description("REST API for managing sales transactions in a supermarket Point of Sale (POS) system. " +
                                "This API handles the complete sales lifecycle including product and customer search, " +
                                "sale creation, item management, multiple payment types (cash and credit), checkout processing, " +
                                "sale freezing, cancellations, and returns (both full and partial).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sales API Support")
                                .email("support@supermarket.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.supermarket.com")
                                .description("Production server")
                ));
    }
}
