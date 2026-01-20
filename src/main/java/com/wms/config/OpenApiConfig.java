package com.wms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for REST API documentation.
 * Accessible at /swagger-ui.html and /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Warehouse Employee Management System API")
                .version("1.0.0")
                .description("Comprehensive API documentation for all modules and endpoints."));
    }
}
