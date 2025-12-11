package com.warehouse.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.api-key.enabled:false}")
    private boolean apiKeyEnabled;
    @Value("${security.api-key.header:X-API-KEY}")
    private String apiKeyHeader;
    @Value("${security.api-key.value:changeme}")
    private String apiKeyValue;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (apiKeyEnabled) {
            http.addFilterBefore(new ApiKeyAuthFilter(apiKeyHeader, apiKeyValue), AbstractPreAuthenticatedProcessingFilter.class);
        }
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Custom authorities mapping if needed
        return converter;
    }
}
