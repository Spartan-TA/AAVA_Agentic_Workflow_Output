package com.wms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter for API key authentication.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String apiKey;
    private static final String API_KEY_HEADER = "X-API-KEY";

    public ApiKeyAuthFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestApiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey.equals(requestApiKey)) {
            Authentication auth = new PreAuthenticatedAuthenticationToken("apiKeyUser", null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid API Key");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
