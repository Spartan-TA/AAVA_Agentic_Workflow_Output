package com.company.warehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables asynchronous processing for background tasks and performance optimization.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Additional async executor beans can be defined here if needed
}
