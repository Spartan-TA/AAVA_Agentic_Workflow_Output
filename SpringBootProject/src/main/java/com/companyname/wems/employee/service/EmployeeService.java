package com.companyname.wems.employee.service;

import com.companyname.wems.employee.model.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;

/**
 * EmployeeService for Employee Master Data CRUD (E02)
 * Implements CRUD, badgeId uniqueness, pagination, filtering, and soft-delete
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Create a new employee with unique badgeId validation
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeId(employee.getBadgeId()).isPresent()) {
            throw new ValidationException("Badge ID must be unique.");
        }
        employee.setStatus("ACTIVE");
        return employeeRepository.save(employee);
    }

    /**
     * Update employee details
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Employee not found."));
        // Only update allowed fields
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setDepartment(updated.getDepartment());
        existing.setPosition(updated.getPosition());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return employeeRepository.save(existing);
    }

    /**
     * Find employee by ID
     */
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Find all employees with pagination
     */
    public Page<Employee> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Find employees by status with pagination
     */
    public Page<Employee> findByStatus(String status, Pageable pageable) {
        return employeeRepository.findByStatus(status, pageable);
    }

    /**
     * Find employees by department with pagination
     */
    public Page<Employee> findByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable);
    }

    /**
     * Soft-delete employee (set status to DELETED)
     */
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Employee not found."));
        employee.setStatus("DELETED");
        employeeRepository.save(employee);
    }
}
