package com.warehouse.ems.employee;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Employee createEmployee(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setName(updated.getName());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        // Only update password if provided
        if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }
}
