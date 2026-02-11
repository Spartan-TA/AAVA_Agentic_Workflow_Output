package com.wms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service for authorization logic and row-level security.
 */
@Service
public class SecurityService {

    /**
     * Get the username of the currently authenticated user.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    /**
     * Check if the current user has a specific role.
     */
    public boolean hasRole(UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role.name()));
        }
        return false;
    }

    /**
     * Row-level security: check if the current user can access a resource owned by the given username.
     */
    public boolean canAccessResource(String resourceOwnerUsername) {
        String currentUsername = getCurrentUsername();
        return currentUsername != null && currentUsername.equals(resourceOwnerUsername);
    }
}
