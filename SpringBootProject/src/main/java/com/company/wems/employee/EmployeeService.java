package com.company.wems.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Get all non-deleted employees with optional pagination.
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Get filtered employees by role and department.
     */
    public List<Employee> filterEmployees(String role, String department) {
        return employeeRepository.filter(role, department);
    }

    /**
     * Get employee by badgeId.
     */
    public Optional<Employee> getByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
    }

    /**
     * Create new employee.
     */
    public Employee createEmployee(Employee employee) {
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    /**
     * Update employee details.
     */
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id).orElseThrow();
        existing.setName(updated.getName());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setShiftGroup(updated.getShiftGroup());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return employeeRepository.save(existing);
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
