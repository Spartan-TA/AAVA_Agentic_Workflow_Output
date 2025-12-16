package com.warehouse.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Employee business logic.
 * Handles CRUD operations, validation, soft-delete, and business rules.
 * 
 * @author Warehouse Development Team
 * @version 1.0.0
 */
@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Retrieve all non-deleted employees with pagination.
     * 
     * @param pageable Pagination information
     * @return Page of employee DTOs
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(EmployeeDTO::fromEntity);
    }

    /**
     * Retrieve an employee by ID.
     * 
     * @param id The employee ID
     * @return Optional containing the employee DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(emp -> !emp.isDeleted())
                .map(EmployeeDTO::fromEntity);
    }

    /**
     * Retrieve an employee by badge ID.
     * 
     * @param badgeId The badge ID
     * @return Optional containing the employee DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeId(badgeId)
                .filter(emp -> !emp.isDeleted())
                .map(EmployeeDTO::fromEntity);
    }

    /**
     * Create a new employee.
     * Validates that the badge ID is unique before creating.
     * 
     * @param employeeDTO The employee data
     * @param createdBy The user creating the employee
     * @return The created employee DTO
     * @throws IllegalArgumentException if badge ID already exists
     */
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO, String createdBy) {
        // Validate unique badge ID
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(employeeDTO.getBadgeId())) {
            throw new IllegalArgumentException(
                "Employee with badge ID " + employeeDTO.getBadgeId() + " already exists"
            );
        }

        Employee employee = employeeDTO.toEntity();
        employee.setCreatedBy(createdBy);
        employee.setUpdatedBy(createdBy);
        employee.setDeleted(false);
        
        if (employee.getStatus() == null || employee.getStatus().isEmpty()) {
            employee.setStatus("ACTIVE");
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(savedEmployee);
    }

    /**
     * Update an existing employee.
     * 
     * @param id The employee ID
     * @param employeeDTO The updated employee data
     * @param updatedBy The user updating the employee
     * @return The updated employee DTO
     * @throws IllegalArgumentException if employee not found or badge ID conflict
     */
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO, String updatedBy) {
        Employee existingEmployee = employeeRepository.findById(id)
                .filter(emp -> !emp.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

        // Check badge ID uniqueness if it's being changed
        if (!existingEmployee.getBadgeId().equals(employeeDTO.getBadgeId())) {
            if (employeeRepository.existsByBadgeIdAndDeletedFalse(employeeDTO.getBadgeId())) {
                throw new IllegalArgumentException(
                    "Employee with badge ID " + employeeDTO.getBadgeId() + " already exists"
                );
            }
        }

        // Update fields
        existingEmployee.setBadgeId(employeeDTO.getBadgeId());
        existingEmployee.setName(employeeDTO.getName());
        existingEmployee.setRole(employeeDTO.getRole());
        existingEmployee.setDepartment(employeeDTO.getDepartment());
        existingEmployee.setShiftGroup(employeeDTO.getShiftGroup());
        existingEmployee.setHireDate(employeeDTO.getHireDate());
        existingEmployee.setStatus(employeeDTO.getStatus());
        existingEmployee.setUpdatedBy(updatedBy);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return EmployeeDTO.fromEntity(updatedEmployee);
    }

    /**
     * Soft-delete an employee.
     * Sets the deleted flag to true instead of physically removing the record.
     * 
     * @param id The employee ID
     * @param deletedBy The user deleting the employee
     * @throws IllegalArgumentException if employee not found
     */
    public void deleteEmployee(Long id, String deletedBy) {
        Employee employee = employeeRepository.findById(id)
                .filter(emp -> !emp.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

        employee.setDeleted(true);
        employee.setStatus("TERMINATED");
        employee.setUpdatedBy(deletedBy);
        employeeRepository.save(employee);
    }

    /**
     * Search employees by multiple criteria with pagination.
     * 
     * @param department Optional department filter
     * @param role Optional role filter
     * @param status Optional status filter
     * @param pageable Pagination information
     * @return Page of employee DTOs matching the criteria
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> searchEmployees(String department, String role, String status, Pageable pageable) {
        return employeeRepository.findByMultipleCriteria(department, role, status, pageable)
                .map(EmployeeDTO::fromEntity);
    }

    /**
     * Get all employees in a specific department.
     * 
     * @param department The department name
     * @return List of employee DTOs in the department
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByDepartment(String department) {
        return employeeRepository.findAllByDepartmentAndDeletedFalse(department)
                .stream()
                .map(EmployeeDTO::fromEntity)
                .toList();
    }

    /**
     * Get all employees with a specific role.
     * 
     * @param role The role name
     * @return List of employee DTOs with the role
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByRole(String role) {
        return employeeRepository.findAllByRoleAndDeletedFalse(role)
                .stream()
                .map(EmployeeDTO::fromEntity)
                .toList();
    }

    /**
     * Get count of employees by department.
     * 
     * @param department The department name
     * @return Number of employees in the department
     */
    @Transactional(readOnly = true)
    public long countEmployeesByDepartment(String department) {
        return employeeRepository.countByDepartmentAndDeletedFalse(department);
    }

    /**
     * Get count of employees by status.
     * 
     * @param status The employment status
     * @return Number of employees with the status
     */
    @Transactional(readOnly = true)
    public long countEmployeesByStatus(String status) {
        return employeeRepository.countByStatusAndDeletedFalse(status);
    }
}