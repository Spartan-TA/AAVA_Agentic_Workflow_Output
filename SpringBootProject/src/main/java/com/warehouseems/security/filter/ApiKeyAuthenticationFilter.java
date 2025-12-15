package com.warehouseems.security.filter;

import com.warehouseems.config.AuthenticationConfig;
import com.warehouseems.security.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * API Key Authentication filter for extracting and validating API keys.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;
    private final AuthenticationConfig authenticationConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!authenticationConfig.isApiKeyAuth() || !authenticationConfig.getApiKey().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String headerName = authenticationConfig.getApiKey().getHeaderName();
        String apiKey = request.getHeader(headerName);
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = apiKeyService.validateApiKey(apiKey);
            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
