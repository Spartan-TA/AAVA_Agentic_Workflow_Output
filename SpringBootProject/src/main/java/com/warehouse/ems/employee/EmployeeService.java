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

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
    }

    public Employee createEmployee(Employee employee) {
        // Validate unique badgeId
        if (employeeRepository.findByBadgeIdAndDeletedFalse(employee.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID already exists.");
        }
        employee.setDeleted(false);
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        if (employee.isDeleted()) {
            throw new IllegalArgumentException("Employee is deleted.");
        }
        employee.setName(updated.getName());
        employee.setRole(updated.getRole());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    public Page<Employee> filterEmployees(String role, String department, Pageable pageable) {
        return employeeRepository.filterByRoleAndDepartment(role, department, pageable);
    }
}
