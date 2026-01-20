package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom queries for employee management.
 * 
 * Extends JpaSpecificationExecutor for dynamic query support.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    
    /**
     * Find an employee by badge ID, excluding soft-deleted records.
     * 
     * @param badgeId the unique badge ID
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    
    /**
     * Find all employees by department, excluding soft-deleted records.
     * 
     * @param department the department name
     * @param pageable pagination information
     * @return Page of employees in the specified department
     */
    Page<Employee> findAllByDepartmentAndDeletedFalse(String department, Pageable pageable);
    
    /**
     * Find all employees by role, excluding soft-deleted records.
     * 
     * @param role the employee role
     * @param pageable pagination information
     * @return Page of employees with the specified role
     */
    Page<Employee> findAllByRoleAndDeletedFalse(String role, Pageable pageable);
    
    /**
     * Find all employees by status, excluding soft-deleted records.
     * 
     * @param status the employment status
     * @param pageable pagination information
     * @return Page of employees with the specified status
     */
    Page<Employee> findAllByStatusAndDeletedFalse(String status, Pageable pageable);
    
    /**
     * Find all active employees (not soft-deleted).
     * 
     * @param pageable pagination information
     * @return Page of active employees
     */
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    /**
     * Count employees by department, excluding soft-deleted records.
     * 
     * @param department the department name
     * @return count of employees in the department
     */
    long countByDepartmentAndDeletedFalse(String department);
    
    /**
     * Find employees by shift group, excluding soft-deleted records.
     * 
     * @param shiftGroup the shift group name
     * @return List of employees in the shift group
     */
    List<Employee> findAllByShiftGroupAndDeletedFalse(String shiftGroup);
    
    /**
     * Check if an employee with the given badge ID exists (excluding soft-deleted).
     * 
     * @param badgeId the badge ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
    
    /**
     * Custom query to search employees by name pattern.
     * 
     * @param namePattern the name pattern to search
     * @param pageable pagination information
     * @return Page of matching employees
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND LOWER(e.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    Page<Employee> searchByName(@Param("namePattern") String namePattern, Pageable pageable);
}