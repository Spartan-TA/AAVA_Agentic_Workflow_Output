package com.warehouseems.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication configuration properties for Warehouse EMS.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthenticationConfig {
    /**
     * Authentication type: 'oauth2' or 'apikey'.
     */
    private String type = "jwt";
    private ApiKeyConfig apiKey = new ApiKeyConfig();

    public boolean isApiKeyAuth() {
        return "apikey".equalsIgnoreCase(type);
    }

    public boolean isOAuth2Auth() {
        return "oauth2".equalsIgnoreCase(type);
    }

    @Data
    public static class ApiKeyConfig {
        /**
         * Header name for API key authentication.
         */
        private String headerName = "X-API-Key";
        /**
         * Whether API key authentication is enabled.
         */
        private boolean enabled = false;
    }
}
