package com.warehouse.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Employee CRUD operations and business logic.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new employee. Enforces unique badgeId.
     */
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(employee.getBadgeId())) {
            throw new IllegalArgumentException("badgeId must be unique");
        }
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    /**
     * Get all non-deleted employees (with pagination).
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(emp -> emp.getDeleted() ? null : emp)
                .filter(emp -> emp != null);
    }

    /**
     * Get employee by badgeId.
     */
    public Optional<Employee> getByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (updated.getBadgeId() != null && !emp.getBadgeId().equals(updated.getBadgeId())) {
            if (employeeRepository.existsByBadgeIdAndDeletedFalse(updated.getBadgeId())) {
                throw new IllegalArgumentException("badgeId must be unique");
            }
            emp.setBadgeId(updated.getBadgeId());
        }
        if (updated.getName() != null) emp.setName(updated.getName());
        if (updated.getRole() != null) emp.setRole(updated.getRole());
        if (updated.getDepartment() != null) emp.setDepartment(updated.getDepartment());
        if (updated.getShiftGroup() != null) emp.setShiftGroup(updated.getShiftGroup());
        if (updated.getHireDate() != null) emp.setHireDate(updated.getHireDate());
        if (updated.getStatus() != null) emp.setStatus(updated.getStatus());
        return employeeRepository.save(emp);
    }

    /**
     * Soft-delete an employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        emp.setDeleted(true);
        employeeRepository.save(emp);
    }
}
