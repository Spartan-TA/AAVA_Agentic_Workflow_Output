package com.wms.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI warehouseEmployeeMgmtOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Warehouse Employee Management System API")
                        .description("Comprehensive API documentation for Warehouse Employee Management System.")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://github.com/your-org/warehouse-employee-mgmt"));
    }
}
