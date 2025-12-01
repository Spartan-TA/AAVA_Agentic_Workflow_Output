package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class for Employee business logic.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    /**
     * Get all employees with pagination and filtering.
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Get employee by ID.
     */
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
    }

    /**
     * Create a new employee.
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Update an existing employee.
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = getEmployee(id);
        employee.setName(updated.getName());
        employee.setBadgeId(updated.getBadgeId());
        employee.setRole(updated.getRole());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        return employeeRepository.save(employee);
    }

    /**
     * Soft delete an employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * Find employee by badge ID.
     */
    public Optional<Employee> findByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }
}
