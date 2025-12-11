package com.clinic.modules.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for E-commerce API documentation.
 * Provides comprehensive API documentation for all e-commerce endpoints.
 */
@Configuration
public class EcommerceOpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clinic E-commerce API")
                        .description("Multi-tenant e-commerce API for clinic management system. " +
                                   "Provides product catalog management, shopping cart, order processing, " +
                                   "and payment capabilities with complete tenant isolation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@clinic-api.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://api.clinic.com")
                                .description("Production server"),
                        new Server()
                                .url("https://staging-api.clinic.com")
                                .description("Staging server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server")))
                .tags(List.of(
                        new Tag()
                                .name("Products (Admin)")
                                .description("Admin endpoints for product catalog management"),
                        new Tag()
                                .name("Products (Public)")
                                .description("Public endpoints for product browsing and search"),
                        new Tag()
                                .name("Categories (Admin)")
                                .description("Admin endpoints for category management"),
                        new Tag()
                                .name("Carousels (Admin)")
                                .description("Admin endpoints for carousel content management"),
                        new Tag()
                                .name("Carousels (Public)")
                                .description("Public endpoints for carousel content display"),
                        new Tag()
                                .name("Shopping Cart")
                                .description("Public endpoints for shopping cart management"),
                        new Tag()
                                .name("Orders")
                                .description("Public endpoints for order processing"),
                        new Tag()
                                .name("Payments")
                                .description("Public endpoints for payment processing"),
                        new Tag()
                                .name("Feature Management")
                                .description("Admin endpoints for e-commerce feature control")));
    }
}
