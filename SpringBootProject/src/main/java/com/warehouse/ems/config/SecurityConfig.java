package com.warehouse.ems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for RBAC, JWT/OAuth2, and API key toggle.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.api-key.enabled:false}")
    private boolean apiKeyEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        // Optionally add API key filter
        if (apiKeyEnabled) {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Custom converter logic for roles mapping if needed
        return converter;
    }
}
