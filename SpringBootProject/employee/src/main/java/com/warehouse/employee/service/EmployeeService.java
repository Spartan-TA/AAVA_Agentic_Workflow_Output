package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
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
    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
    }

    public Employee createEmployee(Employee employee) {
        // Enforce unique badgeId
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(employee.getBadgeId())) {
            throw new IllegalArgumentException("Employee with badgeId already exists");
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id).orElseThrow();
        if (existing.isDeleted()) {
            throw new IllegalStateException("Cannot update deleted employee");
        }
        // Update fields
        existing.setName(updated.getName());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setShiftGroup(updated.getShiftGroup());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return employeeRepository.save(existing);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
