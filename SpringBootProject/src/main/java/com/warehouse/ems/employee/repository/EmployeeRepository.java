package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.domain.EmployeeRole;
import com.warehouse.ems.employee.domain.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom queries for employee management.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find an employee by badge ID (excluding soft-deleted records).
     * 
     * @param badgeId The unique badge identifier
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    
    /**
     * Find all non-deleted employees with pagination.
     * 
     * @param pageable Pagination information
     * @return Page of employees
     */
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    /**
     * Find employees by department (excluding soft-deleted records).
     * 
     * @param department Department name
     * @param pageable Pagination information
     * @return Page of employees in the specified department
     */
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);
    
    /**
     * Find employees by role (excluding soft-deleted records).
     * 
     * @param role Employee role
     * @param pageable Pagination information
     * @return Page of employees with the specified role
     */
    Page<Employee> findByRoleAndDeletedFalse(EmployeeRole role, Pageable pageable);
    
    /**
     * Find employees by status (excluding soft-deleted records).
     * 
     * @param status Employee status
     * @param pageable Pagination information
     * @return Page of employees with the specified status
     */
    Page<Employee> findByStatusAndDeletedFalse(EmployeeStatus status, Pageable pageable);
    
    /**
     * Find employees by shift group (excluding soft-deleted records).
     * 
     * @param shiftGroup Shift group name
     * @return List of employees in the specified shift group
     */
    List<Employee> findByShiftGroupAndDeletedFalse(String shiftGroup);
    
    /**
     * Find employees by tenant ID (for multi-tenant support).
     * 
     * @param tenantId Tenant identifier
     * @param pageable Pagination information
     * @return Page of employees for the specified tenant
     */
    Page<Employee> findByTenantIdAndDeletedFalse(Long tenantId, Pageable pageable);
    
    /**
     * Count active employees in a department.
     * 
     * @param department Department name
     * @param status Employee status
     * @return Count of active employees
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department = :department AND e.status = :status AND e.deleted = false")
    long countByDepartmentAndStatus(@Param("department") String department, @Param("status") EmployeeStatus status);
    
    /**
     * Check if badge ID exists (excluding soft-deleted records).
     * 
     * @param badgeId Badge identifier
     * @return true if badge ID exists
     */
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
    
    /**
     * Find employees by name containing search term (case-insensitive).
     * 
     * @param name Search term
     * @param pageable Pagination information
     * @return Page of matching employees
     */
    Page<Employee> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
}