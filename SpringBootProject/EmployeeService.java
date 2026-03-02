package com.wms.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Service layer for Employee management.
 * Handles business logic, validation, and transaction management.
 * 
 * Key Responsibilities:
 * - Employee CRUD operations
 * - Badge ID uniqueness validation
 * - Soft delete implementation
 * - Multi-criteria filtering
 * - DTO to Entity mapping
 */
@Service
@Transactional
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Constructor-based dependency injection.
     * 
     * @param employeeRepository Repository for employee data access
     */
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    /**
     * Create a new employee.
     * Validates badge ID uniqueness before creation.
     * 
     * @param dto Employee data transfer object
     * @return Created employee entity
     * @throws IllegalArgumentException if badge ID already exists
     */
    public Employee createEmployee(EmployeeDto dto) {
        log.info("Creating employee with badge ID: {}", dto.getBadgeId());
        
        // Validate badge ID uniqueness
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            log.error("Badge ID already exists: {}", dto.getBadgeId());
            throw new IllegalArgumentException("Badge ID already exists: " + dto.getBadgeId());
        }
        
        // Map DTO to entity
        Employee employee = Employee.builder()
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * Get all employees with optional status filtering.
     * Excludes soft-deleted employees.
     * 
     * @param status Optional status filter (null for all statuses)
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(EmployeeStatus status, Pageable pageable) {
        log.debug("Fetching employees with status: {}", status);
        return employeeRepository.findByStatus(status, pageable);
    }
    
    /**
     * Get employee by ID.
     * 
     * @param id Employee ID
     * @return Optional containing employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        log.debug("Fetching employee with ID: {}", id);
        return employeeRepository.findById(id);
    }
    
    /**
     * Get employee by badge ID.
     * 
     * @param badgeId Badge ID
     * @return Optional containing employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        log.debug("Fetching employee with badge ID: {}", badgeId);
        return employeeRepository.findByBadgeId(badgeId);
    }
    
    /**
     * Update an existing employee.
     * 
     * @param id Employee ID
     * @param dto Updated employee data
     * @return Updated employee entity
     * @throws IllegalArgumentException if employee not found
     */
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", id);
                    return new IllegalArgumentException("Employee not found with ID: " + id);
                });
        
        // Update fields
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        
        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", id);
        return updated;
    }
    
    /**
     * Soft delete an employee.
     * Sets the deleted flag to true without removing the record.
     * 
     * @param id Employee ID
     * @throws IllegalArgumentException if employee not found
     */
    public void softDeleteEmployee(Long id) {
        log.info("Soft deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", id);
                    return new IllegalArgumentException("Employee not found with ID: " + id);
                });
        
        employee.setDeleted(true);
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        
        log.info("Employee soft deleted successfully: {}", id);
    }
    
    /**
     * Search employees by multiple criteria.
     * 
     * @param department Optional department filter
     * @param role Optional role filter
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of employees matching criteria
     */
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(
            String department,
            String role,
            EmployeeStatus status,
            Pageable pageable) {
        log.debug("Searching employees with filters - dept: {}, role: {}, status: {}",
                department, role, status);
        return employeeRepository.findByFilters(department, role, status, pageable);
    }
    
    /**
     * Get count of active employees by status.
     * 
     * @param status Employee status
     * @return Count of employees
     */
    @Transactional(readOnly = true)
    public long countByStatus(EmployeeStatus status) {
        return employeeRepository.countByStatus(status);
    }
}
