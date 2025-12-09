package com.warehouse.ems.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for Warehouse EMS.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Employee Management System API")
                        .version("1.0.0")
                        .description("API documentation for Warehouse EMS.")
                        .contact(new Contact()
                                .name("Warehouse EMS Team")
                                .email("support@warehouseems.com")
                        )
                );
    }
}
