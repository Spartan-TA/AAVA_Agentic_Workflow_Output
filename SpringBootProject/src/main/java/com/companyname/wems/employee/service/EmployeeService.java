package com.companyname.wems.employee.service;

import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.exception.DuplicateResourceException;
import com.companyname.wems.exception.ResourceNotFoundException;
import com.companyname.wems.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Employee management
 * 
 * Provides business logic for employee CRUD operations including:
 * - Employee creation with validation
 * - Employee updates with audit trail
 * - Soft delete functionality
 * - Filtering and pagination
 * - Badge ID uniqueness enforcement
 * 
 * All write operations are transactional to ensure data consistency.
 * Logging is implemented for audit and troubleshooting purposes.
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new employee
     * 
     * Validates badge ID uniqueness before creation.
     * Sets initial status to ACTIVE.
     * Logs creation for audit purposes.
     * 
     * @param employee Employee entity to create
     * @return Created employee with generated ID
     * @throws DuplicateResourceException if badge ID already exists
     */
    public Employee createEmployee(Employee employee) {
        logger.info("Creating employee with badge ID: {}", employee.getBadgeId());
        
        // Validate badge ID uniqueness
        if (employeeRepository.existsByBadgeId(employee.getBadgeId())) {
            logger.warn("Duplicate badge ID detected: {}", employee.getBadgeId());
            throw new DuplicateResourceException(
                String.format("Employee with badge ID '%s' already exists", employee.getBadgeId())
            );
        }
        
        // Ensure status is set
        if (employee.getStatus() == null) {
            employee.setStatus(Employee.Status.ACTIVE);
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Successfully created employee: {} (ID: {})", 
                   savedEmployee.getName(), savedEmployee.getId());
        
        return savedEmployee;
    }

    /**
     * Update an existing employee
     * 
     * Updates all modifiable fields while preserving audit information.
     * Badge ID cannot be changed after creation.
     * 
     * @param id Employee ID to update
     * @param updated Employee entity with updated values
     * @return Updated employee
     * @throws ResourceNotFoundException if employee not found
     */
    public Employee updateEmployee(Long id, Employee updated) {
        logger.info("Updating employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException(
                        String.format("Employee not found with ID: %d", id)
                    );
                });
        
        // Update modifiable fields
        employee.setName(updated.getName());
        employee.setRole(updated.getRole());
        employee.setDepartment(updated.getDepartment());
        employee.setShiftGroup(updated.getShiftGroup());
        employee.setHireDate(updated.getHireDate());
        employee.setStatus(updated.getStatus());
        
        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Successfully updated employee: {} (ID: {})", 
                   savedEmployee.getName(), savedEmployee.getId());
        
        return savedEmployee;
    }

    /**
     * Get employee by ID
     * 
     * @param id Employee ID
     * @return Employee entity
     * @throws ResourceNotFoundException if employee not found
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Employee getEmployee(Long id) {
        logger.debug("Fetching employee with ID: {}", id);
        
        return employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException(
                        String.format("Employee not found with ID: %d", id)
                    );
                });
    }

    /**
     * Get employee by badge ID
     * 
     * Used for clock-in/out operations and authentication
     * 
     * @param badgeId Unique badge identifier
     * @return Employee entity
     * @throws ResourceNotFoundException if employee not found
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Employee getEmployeeByBadgeId(String badgeId) {
        logger.debug("Fetching employee with badge ID: {}", badgeId);
        
        return employeeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> {
                    logger.error("Employee not found with badge ID: {}", badgeId);
                    return new ResourceNotFoundException(
                        String.format("Employee not found with badge ID: %s", badgeId)
                    );
                });
    }

    /**
     * List all employees with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Employee> listEmployees(Pageable pageable) {
        logger.debug("Listing employees with pagination: {}", pageable);
        return employeeRepository.findAll(pageable);
    }

    /**
     * List employees filtered by department and status
     * 
     * @param department Department name (optional)
     * @param status Employee status (optional)
     * @param pageable Pagination parameters
     * @return Page of filtered employees
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Employee> listEmployees(String department, Employee.Status status, Pageable pageable) {
        logger.debug("Listing employees - Department: {}, Status: {}", department, status);
        
        if (department != null && status != null) {
            return employeeRepository.findByDepartmentAndStatus(department, status, pageable);
        } else if (department != null) {
            return employeeRepository.findByDepartment(department, pageable);
        } else if (status != null) {
            return employeeRepository.findByStatus(status, pageable);
        } else {
            return employeeRepository.findAll(pageable);
        }
    }

    /**
     * Soft delete an employee
     * 
     * Sets status to TERMINATED instead of physically deleting the record.
     * Preserves historical data for audit and reporting purposes.
     * 
     * @param id Employee ID to delete
     * @throws ResourceNotFoundException if employee not found
     */
    public void deleteEmployee(Long id) {
        logger.info("Soft-deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException(
                        String.format("Employee not found with ID: %d", id)
                    );
                });
        
        employee.setStatus(Employee.Status.TERMINATED);
        employeeRepository.save(employee);
        
        logger.info("Successfully soft-deleted employee: {} (ID: {})", 
                   employee.getName(), employee.getId());
    }

    /**
     * Search employees by name pattern
     * 
     * @param namePattern Name pattern to search (case-insensitive)
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Employee> searchEmployeesByName(String namePattern, Pageable pageable) {
        logger.debug("Searching employees by name pattern: {}", namePattern);
        return employeeRepository.searchByName(namePattern, pageable);
    }

    /**
     * Get active employees by department
     * 
     * @param department Department name
     * @return List of active employees in department
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Employee> getActiveEmployeesByDepartment(String department) {
        logger.debug("Fetching active employees for department: {}", department);
        return employeeRepository.findActiveEmployeesByDepartment(department);
    }

    /**
     * Get employee count by department
     * 
     * @param department Department name
     * @return Count of employees in department
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public long getEmployeeCountByDepartment(String department) {
        logger.debug("Counting employees in department: {}", department);
        return employeeRepository.countByDepartment(department);
    }

    /**
     * Get employee count by status
     * 
     * @param status Employee status
     * @return Count of employees with status
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public long getEmployeeCountByStatus(Employee.Status status) {
        logger.debug("Counting employees with status: {}", status);
        return employeeRepository.countByStatus(status);
    }

    /**
     * Validate employee exists and is active
     * 
     * @param id Employee ID
     * @throws BusinessException if employee not found or not active
     */
    public void validateEmployeeActive(Long id) {
        Employee employee = getEmployee(id);
        if (employee.getStatus() != Employee.Status.ACTIVE) {
            throw new BusinessException(
                String.format("Employee %s is not active (Status: %s)", 
                             employee.getName(), employee.getStatus())
            );
        }
    }
}