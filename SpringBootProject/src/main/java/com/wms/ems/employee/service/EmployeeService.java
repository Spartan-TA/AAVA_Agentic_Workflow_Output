package com.wms.ems.employee.service;

import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Employee business logic.
 * Handles CRUD operations, soft delete, and validation.
 */
@Service
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all active (not deleted) employees.
     * @return List of Employee
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAllByDeletedFalse();
    }

    /**
     * Get an employee by ID.
     * @param id the employee ID
     * @return Optional of Employee
     */
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
    }

    /**
     * Create a new employee after validation.
     * @param employee the employee to create
     * @return the saved Employee
     */
    public Employee createEmployee(Employee employee) {
        validateEmployee(employee);
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    /**
     * Update an existing employee after validation.
     * @param id the employee ID
     * @param updated the updated employee data
     * @return the updated Employee
     */
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        validateEmployee(updated);
        existing.setName(updated.getName());
        existing.setBadgeId(updated.getBadgeId());
        existing.setDepartment(updated.getDepartment());
        existing.setEmail(updated.getEmail());
        // ... update other fields as needed
        return employeeRepository.save(existing);
    }

    /**
     * Soft delete an employee (mark as deleted).
     * @param id the employee ID
     */
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * Validate employee data before saving.
     * Throws IllegalArgumentException if invalid.
     * @param employee the employee to validate
     */
    private void validateEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (employee.getBadgeId() == null || employee.getBadgeId().isBlank()) {
            throw new IllegalArgumentException("Badge ID is required");
        }
        // Add more validation as needed
    }
}
