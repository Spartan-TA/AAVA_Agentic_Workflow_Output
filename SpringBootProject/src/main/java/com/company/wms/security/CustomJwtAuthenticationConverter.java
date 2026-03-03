package com.company.wms.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom JWT authentication converter to extract roles from JWT claims.
 * Converts JWT roles to Spring Security GrantedAuthority format.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = 
        new JwtGrantedAuthoritiesConverter();

    /**
     * Convert JWT to authentication token with extracted roles
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            defaultGrantedAuthoritiesConverter.convert(jwt).stream(),
            extractRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extract roles from JWT claims and convert to GrantedAuthority
     */
    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        // Extract roles from 'roles' claim in JWT
        Collection<String> roles = jwt.getClaimAsStringList("roles");
        
        if (roles == null || roles.isEmpty()) {
            // Fallback to 'role' claim if 'roles' not present
            String role = jwt.getClaimAsString("role");
            if (role != null) {
                return Stream.of(role)
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toSet());
            }
            return Stream.empty().collect(Collectors.toSet());
        }
        
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
    }
}