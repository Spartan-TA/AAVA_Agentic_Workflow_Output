package com.warehouse.ems.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

/**
 * Flyway configuration for Warehouse EMS.
 * Ensures database migrations are applied on startup.
 */
@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    @FlywayDataSource
    public Flyway flyway(DataSource dataSource,
                         @Value("${spring.flyway.locations}") String locations) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(true)
                .load();
    }
}
