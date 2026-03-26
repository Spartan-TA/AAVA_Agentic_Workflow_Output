package com.warehouse.employee.management.service;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String badgeId) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                employee.getBadgeId(),
                "", // Password not stored here; use external IDP or HRIS for real auth
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole()))
        );
    }
}