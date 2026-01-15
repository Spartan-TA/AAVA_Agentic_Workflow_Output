package com.company.wms.security.service;

import com.company.wms.security.model.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads user details for authentication and RBAC.
 * In production, replace with DB-backed implementation.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Demo: hardcoded users. Replace with DB lookup.
        if ("admin".equals(username)) {
            return new User("admin", "$2a$10$7QJQwQwQwQwQwQwQwQwQwOeQwQwQwQwQwQwQwQwQwQwQwQwQwQwQw", // bcrypt: "password"
                    getAuthorities(Arrays.asList(Role.ADMIN, Role.EMPLOYEE_MANAGER)));
        } else if ("employee".equals(username)) {
            return new User("employee", "$2a$10$7QJQwQwQwQwQwQwQwQwQwOeQwQwQwQwQwQwQwQwQwQwQwQwQwQw", // bcrypt: "password"
                    getAuthorities(List.of(Role.EMPLOYEE)));
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }

    private List<SimpleGrantedAuthority> getAuthorities(List<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }
}
