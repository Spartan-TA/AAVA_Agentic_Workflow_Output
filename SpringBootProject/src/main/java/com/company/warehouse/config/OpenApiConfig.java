// src/main/java/com/company/warehouse/config/OpenApiConfig.java
package com.company.warehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI warehouseOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Warehouse Employee Management API")
                .description("API documentation for warehouse employee management system")
                .version("1.0.0"));
    }
}