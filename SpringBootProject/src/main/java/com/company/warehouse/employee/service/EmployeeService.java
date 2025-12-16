package com.company.warehouse.employee.service;

import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.employee.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Service for Employee CRUD operations and business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new employee.
     */
    public Employee createEmployee(CreateEmployeeDto dto, String tenantId) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID must be unique.");
        }
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status("ACTIVE")
                .tenantId(tenantId)
                .build();
        return employeeRepository.save(employee);
    }

    /**
     * Get paginated list of employees for a tenant.
     */
    public Page<Employee> getEmployees(String tenantId, Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Get employee by ID.
     */
    public Optional<Employee> getEmployee(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Update employee details.
     */
    public Employee updateEmployee(Long id, UpdateEmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        if (StringUtils.hasText(dto.getName())) employee.setName(dto.getName());
        if (StringUtils.hasText(dto.getDepartment())) employee.setDepartment(dto.getDepartment());
        if (StringUtils.hasText(dto.getRole())) employee.setRole(dto.getRole());
        if (StringUtils.hasText(dto.getShiftGroup())) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        return employeeRepository.save(employee);
    }

    /**
     * Soft-delete employee (set status to INACTIVE).
     */
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }
}
