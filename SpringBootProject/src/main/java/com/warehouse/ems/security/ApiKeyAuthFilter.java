package com.warehouse.ems.security;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String VALID_API_KEY = "your-api-key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && apiKey.equals(VALID_API_KEY)) {
            // Set authentication context if needed
        }
        filterChain.doFilter(request, response);
    }
}