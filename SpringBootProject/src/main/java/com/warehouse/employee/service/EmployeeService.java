package com.warehouse.employee.service;

import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.audit.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service layer for Employee business logic.
 * Handles all employee-related operations including CRUD, search, and validation.
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Creates a new employee with validation.
     * 
     * @param employee Employee entity to create
     * @return Created employee
     * @throws IllegalArgumentException if badge ID already exists
     */
    @Transactional
    public Employee create(Employee employee) {
        log.info("Creating new employee with badge ID: {}", employee.getBadgeId());
        
        // Validate unique badge ID
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(employee.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists: " + employee.getBadgeId());
        }
        
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setDeleted(false);
        
        Employee saved = employeeRepository.save(employee);
        auditService.logCreate("Employee", saved.getId(), saved);
        
        log.info("Successfully created employee with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing employee.
     * 
     * @param id Employee ID
     * @param updatedEmployee Updated employee data
     * @return Updated employee
     * @throws IllegalArgumentException if employee not found
     */
    @Transactional
    public Employee update(Long id, Employee updatedEmployee) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        
        if (employee.isDeleted()) {
            throw new IllegalArgumentException("Cannot update deleted employee");
        }
        
        Employee beforeState = Employee.builder()
            .id(employee.getId())
            .badgeId(employee.getBadgeId())
            .name(employee.getName())
            .role(employee.getRole())
            .department(employee.getDepartment())
            .build();
        
        // Update fields
        employee.setName(updatedEmployee.getName());
        employee.setRole(updatedEmployee.getRole());
        employee.setDepartment(updatedEmployee.getDepartment());
        employee.setShiftGroup(updatedEmployee.getShiftGroup());
        employee.setStatus(updatedEmployee.getStatus());
        employee.setEmail(updatedEmployee.getEmail());
        employee.setPhoneNumber(updatedEmployee.getPhoneNumber());
        employee.setUpdatedAt(LocalDateTime.now());
        
        Employee saved = employeeRepository.save(employee);
        auditService.logUpdate("Employee", saved.getId(), beforeState, saved);
        
        log.info("Successfully updated employee with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Soft deletes an employee (sets deleted flag to true).
     * 
     * @param id Employee ID
     * @throws IllegalArgumentException if employee not found
     */
    @Transactional
    public void softDelete(Long id) {
        log.info("Soft deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        
        if (employee.isDeleted()) {
            throw new IllegalArgumentException("Employee already deleted");
        }
        
        employee.setDeleted(true);
        employee.setStatus("TERMINATED");
        employee.setUpdatedAt(LocalDateTime.now());
        
        employeeRepository.save(employee);
        auditService.logDelete("Employee", id, employee);
        
        log.info("Successfully soft deleted employee with ID: {}", id);
    }

    /**
     * Retrieves all active employees with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    @Transactional(readOnly = true)
    public Page<Employee> list(Pageable pageable) {
        log.debug("Listing employees with pagination: {}", pageable);
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * Searches employees by multiple criteria.
     * 
     * @param name Name filter (optional)
     * @param department Department filter (optional)
     * @param role Role filter (optional)
     * @param status Status filter (optional)
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    @Transactional(readOnly = true)
    public Page<Employee> search(String name, String department, String role, String status, Pageable pageable) {
        log.debug("Searching employees with filters - name: {}, department: {}, role: {}, status: {}", 
                  name, department, role, status);
        return employeeRepository.searchEmployees(name, department, role, status, pageable);
    }

    /**
     * Finds an employee by badge ID.
     * 
     * @param badgeId Badge identifier
     * @return Optional containing employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findByBadgeId(String badgeId) {
        log.debug("Finding employee by badge ID: {}", badgeId);
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
    }

    /**
     * Finds an employee by ID.
     * 
     * @param id Employee ID
     * @return Optional containing employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long id) {
        log.debug("Finding employee by ID: {}", id);
        return employeeRepository.findById(id)
            .filter(emp -> !emp.isDeleted());
    }

    /**
     * Gets count of active employees.
     * 
     * @return Count of active employees
     */
    @Transactional(readOnly = true)
    public long countActiveEmployees() {
        return employeeRepository.countByDeletedFalse();
    }

    /**
     * Gets count of employees by department.
     * 
     * @param department Department name
     * @return Count of employees in department
     */
    @Transactional(readOnly = true)
    public long countByDepartment(String department) {
        return employeeRepository.countByDepartmentAndDeletedFalse(department);
    }

    /**
     * Validates if badge ID is available.
     * 
     * @param badgeId Badge identifier to check
     * @return true if available, false if already exists
     */
    @Transactional(readOnly = true)
    public boolean isBadgeIdAvailable(String badgeId) {
        return !employeeRepository.existsByBadgeIdAndDeletedFalse(badgeId);
    }
}