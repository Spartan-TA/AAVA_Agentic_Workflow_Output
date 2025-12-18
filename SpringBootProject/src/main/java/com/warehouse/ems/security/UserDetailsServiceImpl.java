package com.warehouse.ems.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import java.util.Collections;

/**
 * UserDetailsService implementation for loading user details from Employee entity.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(employee.getUsername())
                .password(employee.getPassword())
                .roles(employee.getRole().name())
                .authorities(Collections.emptyList())
                .build();
    }
}
