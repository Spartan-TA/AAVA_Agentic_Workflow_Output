package com.warehouse.employee.management.service.impl;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import com.warehouse.employee.management.service.EmployeeService;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of EmployeeService for business logic.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByBadgeId(employee.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique.");
        }
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found."));
        // Update fields
        existing.setName(employee.getName());
        existing.setRole(employee.getRole());
        existing.setDepartment(employee.getDepartment());
        existing.setShiftGroup(employee.getShiftGroup());
        existing.setHireDate(employee.getHireDate());
        existing.setStatus(employee.getStatus());
        return employeeRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found."));
        employeeRepository.delete(existing); // Soft-delete via @SQLDelete
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> listEmployees(String department, int page, int size) {
        return employeeRepository.findAllActiveByDepartment(department);
    }
}
