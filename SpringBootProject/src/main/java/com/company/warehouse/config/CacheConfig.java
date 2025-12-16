package com.company.warehouse.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables caching for performance optimization.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Additional cache manager beans can be defined here if needed
}
