package com.company.wems.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Implement user loading from DB or external provider
        throw new UsernameNotFoundException("User not found");
    }
}