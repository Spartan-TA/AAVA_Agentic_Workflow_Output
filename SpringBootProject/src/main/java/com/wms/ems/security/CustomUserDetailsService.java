package com.wms.ems.security;

import com.wms.ems.employee.EmployeeRepository;
import com.wms.ems.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByBadgeId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with badgeId: " + username));
        return new org.springframework.security.core.userdetails.User(
                employee.getBadgeId(),
                employee.getPassword(),
                getAuthorities(employee.getRole())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
