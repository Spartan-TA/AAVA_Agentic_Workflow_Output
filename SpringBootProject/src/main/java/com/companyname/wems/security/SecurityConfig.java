package com.companyname.wems.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig for Role-Based Access Control (E03)
 * Supports API key and OAuth2 authentication toggle
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${security.auth-type:apikey}")
    private String authType;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if ("oauth2".equalsIgnoreCase(authType)) {
            http
                .authorizeRequests()
                    .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .anyRequest().authenticated()
                .and()
                    .oauth2ResourceServer().jwt();
        } else {
            http
                .authorizeRequests()
                    .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .anyRequest().authenticated()
                .and()
                    .httpBasic();
        }
        http.csrf().disable();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserDetailsService bean would be defined here for user/role management
}
