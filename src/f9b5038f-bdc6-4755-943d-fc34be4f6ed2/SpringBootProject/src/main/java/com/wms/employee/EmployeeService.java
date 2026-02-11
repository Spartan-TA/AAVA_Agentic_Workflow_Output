package com.wms.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee CRUD operations.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeId(employee.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        existing.setBadgeId(updated.getBadgeId());
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
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        existing.setDeleted(true);
        employeeRepository.save(existing);
    }
}
