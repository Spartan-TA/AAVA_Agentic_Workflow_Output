package com.warehouseems.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for Warehouse EMS API documentation.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI warehouseEmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse EMS API")
                        .version("1.0.0")
                        .description("Spring Boot application for warehouse employee management.")
                        .contact(new Contact()
                                .name("Warehouse EMS Team")
                                .email("support@warehouseems.com")
                                .url("https://warehouseems.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development server"),
                        new Server().url("https://warehouseems.com").description("Production server")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }
}
