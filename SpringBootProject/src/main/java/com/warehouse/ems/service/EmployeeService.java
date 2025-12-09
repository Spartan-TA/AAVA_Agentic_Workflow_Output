package com.warehouse.ems.service;

import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic.
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
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by ID.
     */
    public Optional<Employee> getEmployee(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
    }

    /**
     * Get paginated list of active employees.
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(e -> e.isDeleted() ? null : e);
    }

    /**
     * Update employee details.
     */
    public Employee updateEmployee(Long id, Employee updated) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setName(updated.getName());
            employee.setDepartment(updated.getDepartment());
            employee.setRole(updated.getRole());
            employee.setShiftGroup(updated.getShiftGroup());
            employee.setStatus(updated.getStatus());
            return employeeRepository.save(employee);
        }).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    /**
     * Soft-delete employee.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
        });
    }
}
