package com.warehouse.ems.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new employee, enforcing unique badgeId.
     */
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeIdAndDeletedFalse(employee.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique.");
        }
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by ID (not deleted).
     */
    public Optional<Employee> getEmployee(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
    }

    /**
     * Update employee details.
     */
    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = getEmployee(id).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setName(updated.getName());
        employee.setRole(updated.getRole());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        return employeeRepository.save(employee);
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployee(id).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * Patch employee status.
     */
    public Employee patchStatus(Long id, String status) {
        Employee employee = getEmployee(id).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setStatus(status);
        return employeeRepository.save(employee);
    }

    /**
     * List employees with pagination and filtering.
     */
    public Page<Employee> listEmployees(String name, String department, String role, Pageable pageable) {
        return employeeRepository.filterEmployees(name, department, role, pageable);
    }
}
