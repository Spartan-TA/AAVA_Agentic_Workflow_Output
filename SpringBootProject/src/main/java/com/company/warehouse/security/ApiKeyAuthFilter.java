package com.company.warehouse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Custom filter for API Key authentication.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final String headerName;
    private final List<String> validApiKeys;

    public ApiKeyAuthFilter(String headerName, List<String> validApiKeys) {
        this.headerName = headerName;
        this.validApiKeys = validApiKeys;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(headerName);
        if (StringUtils.hasText(apiKey) && validApiKeys.contains(apiKey)) {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "apiKeyUser", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
