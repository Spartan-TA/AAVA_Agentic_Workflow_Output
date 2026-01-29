package com.company.wms.config;

import com.company.wms.common.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration with RBAC.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/scheduling/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/safety/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/audit/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // For demo purposes, use in-memory users. Replace with persistent user store in production.
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password("{noop}adminpass").roles(Role.ADMIN.name()).build(),
            User.withUsername("hr").password("{noop}hrpass").roles(Role.HR.name()).build(),
            User.withUsername("supervisor").password("{noop}supervisorpass").roles(Role.SUPERVISOR.name()).build(),
            User.withUsername("worker").password("{noop}workerpass").roles(Role.WORKER.name()).build()
        );
    }
}
