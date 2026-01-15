package com.company.wms.employee.service;

import com.company.wms.employee.domain.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing Employee entities.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new employee.
     */
    @Transactional
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by ID (not deleted).
     */
    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    /**
     * Update employee details.
     */
    @Transactional
    public Employee update(Long id, Employee updated) {
        Employee existing = getById(id);
        existing.setName(updated.getName());
        existing.setBadgeId(updated.getBadgeId());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setShiftGroup(updated.getShiftGroup());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return employeeRepository.save(existing);
    }

    /**
     * Soft-delete an employee.
     */
    @Transactional
    public void delete(Long id) {
        Employee employee = getById(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    /**
     * List employees with pagination and optional search.
     */
    @Transactional(readOnly = true)
    public Page<Employee> search(String name, String department, String role, Pageable pageable) {
        return employeeRepository.search(name, department, role, pageable);
    }

    /**
     * List all employees (not deleted) with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Employee> list(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }
}
