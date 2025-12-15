package com.warehouseems.employee.service;

import com.warehouseems.employee.dto.EmployeeDto;
import com.warehouseems.employee.entity.Employee;
import com.warehouseems.employee.repository.EmployeeRepository;
import com.warehouseems.employee.repository.EmployeeSpecification;
import com.warehouseems.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 * Handles CRUD, soft delete, filtering, and pagination.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new employee.
     */
    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID must be unique");
        }
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        return employeeRepository.save(employee);
    }

    /**
     * Get all employees with optional filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(String department, String role, String status, LocalDate hireStart, LocalDate hireEnd, Pageable pageable) {
        Specification<Employee> spec = Specification.where(EmployeeSpecification.hasDepartment(department))
                .and(EmployeeSpecification.hasRole(role))
                .and(EmployeeSpecification.hasStatus(status))
                .and(EmployeeSpecification.hireDateBetween(hireStart, hireEnd));
        return employeeRepository.findAll(spec, pageable);
    }

    /**
     * Get employee by ID.
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    /**
     * Update employee by ID.
     */
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = getEmployeeById(id);
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    /**
     * Soft delete employee by ID.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
