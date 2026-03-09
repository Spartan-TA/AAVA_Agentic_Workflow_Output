package com.company.warehouse.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway migration configuration for Warehouse EMS.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(@Value("${spring.datasource.url}") String url,
                         @Value("${spring.datasource.username}") String user,
                         @Value("${spring.datasource.password}") String password) {
        return Flyway.configure()
                .dataSource(url, user, password)
                .baselineOnMigrate(true)
                .load();
    }
}
