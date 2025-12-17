package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.audit.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Employee entity.
 * Implements business logic, validation, exception handling, and audit logging.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Get all active employees.
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAllActive();
    }

    /**
     * Get employee by ID.
     */
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    /**
     * Create a new employee.
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        Employee saved = employeeRepository.save(employee);
        auditService.logCreate("Employee", saved.getId(), saved);
        return saved;
    }

    /**
     * Update an existing employee.
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existing = getEmployeeById(id);
        // Update fields
        existing.setName(updatedEmployee.getName());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setDepartment(updatedEmployee.getDepartment());
        // ... other fields
        Employee saved = employeeRepository.save(existing);
        auditService.logUpdate("Employee", saved.getId(), saved);
        return saved;
    }

    /**
     * Soft-delete an employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee existing = getEmployeeById(id);
        existing.setDeletedAt(java.time.LocalDateTime.now());
        employeeRepository.save(existing);
        auditService.logDelete("Employee", existing.getId());
    }
}
