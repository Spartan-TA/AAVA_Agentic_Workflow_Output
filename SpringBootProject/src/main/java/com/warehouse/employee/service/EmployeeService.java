package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Create a new employee.
     */
    public Employee createEmployee(Employee employee) {
        // Validate unique badgeId
        if (employeeRepository.findByBadgeId(employee.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID already exists.");
        }
        return employeeRepository.save(employee);
    }

    /**
     * Get all active employees.
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByDeletedFalse();
    }

    /**
     * Get employee by ID.
     */
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        if (updated.getName() != null) employee.setName(updated.getName());
        if (updated.getRole() != null) employee.setRole(updated.getRole());
        if (updated.getDepartment() != null) employee.setDepartment(updated.getDepartment());
        if (updated.getShiftGroup() != null) employee.setShiftGroup(updated.getShiftGroup());
        if (updated.getHireDate() != null) employee.setHireDate(updated.getHireDate());
        if (updated.getStatus() != null) employee.setStatus(updated.getStatus());
        return employeeRepository.save(employee);
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
