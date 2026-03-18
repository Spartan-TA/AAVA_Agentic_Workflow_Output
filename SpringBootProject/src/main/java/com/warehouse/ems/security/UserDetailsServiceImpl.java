package com.warehouse.ems.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for loading user details from DB.
 * Replace with actual repository integration for production.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Integrate with EmployeeRepository or UserRepository
        // For demonstration, return a dummy user
        if ("admin".equals(username)) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username("admin")
                    .password("$2a$10$dummyhashedpassword") // BCrypt hash
                    .roles("ADMIN")
                    .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
