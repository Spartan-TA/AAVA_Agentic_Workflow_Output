package com.warehouse.ems.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for Swagger UI documentation.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI warehouseEmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Employee Management System API")
                        .description("Comprehensive API documentation for Warehouse EMS")
                        .version("1.0.0"));
    }
}
