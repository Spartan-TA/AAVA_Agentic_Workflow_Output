package com.warehouseems.security.util;

import com.warehouseems.security.entity.User;
import com.warehouseems.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for security-related operations.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    /**
     * Get the current authenticated User entity.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.orElse(null);
    }

    /**
     * Check if the current user has the specified role.
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(role) || r.equals("ROLE_" + role));
    }

    /**
     * Get the department of the current user.
     */
    public String getCurrentUserDepartment() {
        User user = getCurrentUser();
        return user != null ? user.getDepartment() : null;
    }
}
