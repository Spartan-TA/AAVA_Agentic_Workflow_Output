package com.example.mcqassessment.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/assessments/**").hasAnyRole("TEACHER", "CURRICULUM_PLANNER", "STUDENT")
                .antMatchers("/api/attempts/**").hasRole("STUDENT")
                .antMatchers("/api/performance/**").hasAnyRole("TEACHER", "CURRICULUM_PLANNER")
                .antMatchers("/api/export/**").hasAnyRole("TEACHER", "CURRICULUM_PLANNER")
                .anyRequest().authenticated()
            .and()
            .headers().frameOptions().disable(); // for H2 console
        return http.build();
    }
}
