package com.warehouse.employeemgmt.service;

import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.dto.EmployeeDTO;
import com.warehouse.employeemgmt.repository.EmployeeRepository;
import com.warehouse.employeemgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EmployeeService - Business logic for employee management
 * 
 * Handles all employee-related operations including CRUD, validation,
 * soft-delete, filtering, and pagination.
 * 
 * Features:
 * - Transactional operations
 * - Badge ID uniqueness validation
 * - Soft-delete support
 * - Department and role filtering
 * - Pagination support
 * - Audit logging integration
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;

    /**
     * Get all active employees with pagination
     */
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        log.info("Fetching all active employees with pagination");
        return employeeRepository.findAll(pageable);
    }

    /**
     * Get employee by badge ID
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByBadgeId(String badgeId) {
        log.info("Fetching employee with badge ID: {}", badgeId);
        return employeeRepository.findByBadgeIdAndSoftDeletedFalse(badgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with badge ID: " + badgeId));
    }

    /**
     * Get employee by ID
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
    }

    /**
     * Create new employee
     */
    @Transactional
    public Employee createEmployee(EmployeeDTO dto) {
        log.info("Creating new employee with badge ID: {}", dto.getBadgeId());
        
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists: " + dto.getBadgeId());
        }
        
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status("ACTIVE")
                .softDeleted(false)
                .build();
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing employee
     */
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = getEmployeeById(id);
        
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        
        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully with ID: {}", updated.getId());
        return updated;
    }

    /**
     * Soft delete employee
     */
    @Transactional
    public void softDeleteEmployee(Long id) {
        log.info("Soft deleting employee with ID: {}", id);
        
        Employee employee = getEmployeeById(id);
        employee.setSoftDeleted(true);
        employee.setStatus("TERMINATED");
        
        employeeRepository.save(employee);
        log.info("Employee soft deleted successfully with ID: {}", id);
    }

    /**
     * Find employees by department
     */
    @Transactional(readOnly = true)
    public List<Employee> findByDepartment(String department) {
        log.info("Finding employees in department: {}", department);
        return employeeRepository.findByDepartmentAndSoftDeletedFalse(department);
    }

    /**
     * Find employees by role
     */
    @Transactional(readOnly = true)
    public List<Employee> findByRole(String role) {
        log.info("Finding employees with role: {}", role);
        return employeeRepository.findByRoleAndSoftDeletedFalse(role);
    }

    /**
     * Search employees by name
     */
    @Transactional(readOnly = true)
    public List<Employee> searchByName(String name) {
        log.info("Searching employees by name: {}", name);
        return employeeRepository.searchByName(name);
    }
}