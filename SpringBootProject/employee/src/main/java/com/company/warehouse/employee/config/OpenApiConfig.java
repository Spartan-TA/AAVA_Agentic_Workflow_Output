package com.company.warehouse.employee.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI warehouseOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Warehouse Employee Management API")
                        .description("API documentation for Warehouse Employee Management System")
                        .version("1.0.0"));
    }

    @Bean
    public GroupedOpenApi employeeApi() {
        return GroupedOpenApi.builder()
                .group("employee")
                .pathsToMatch("/api/employees/**")
                .build();
    }
}