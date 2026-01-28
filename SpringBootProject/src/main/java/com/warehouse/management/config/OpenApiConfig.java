package com.warehouse.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for Swagger UI.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI warehouseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Employee Management API")
                        .version("1.0.0")
                        .description("API documentation for Warehouse Employee Management System"));
    }
}