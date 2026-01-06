package com.companyname.warehouse.security.service;

import com.companyname.warehouse.employee.entity.Employee;
import com.companyname.warehouse.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Loads user details for authentication and RBAC.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + email));
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + employee.getRole().name());
        return new org.springframework.security.core.userdetails.User(
                employee.getEmail(),
                "", // Password is not used for JWT stateless auth
                Collections.singletonList(authority)
        );
    }
}
