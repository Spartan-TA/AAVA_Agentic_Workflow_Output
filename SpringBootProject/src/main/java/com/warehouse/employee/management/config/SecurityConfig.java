package com.warehouse.employee.management.config;

import com.warehouse.employee.management.security.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for RBAC and API key/OAuth2 toggle.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${security.api-key.enabled}")
    private boolean apiKeyEnabled;
    @Value("${security.api-key.value}")
    private String apiKeyValue;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/swagger-ui.html", "/api-docs/**", "/actuator/**").permitAll()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            );
        if (apiKeyEnabled) {
            http.addFilterBefore(new ApiKeyAuthFilter(apiKeyValue), UsernamePasswordAuthenticationFilter.class);
        }
        // OAuth2/JWT config can be added here if needed
        return http.build();
    }
}
