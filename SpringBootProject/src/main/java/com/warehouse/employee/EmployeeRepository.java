package com.warehouse.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity data access operations.
 * Extends JpaRepository to provide standard CRUD operations and custom query methods.
 * 
 * @author Warehouse Development Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find an employee by their unique badge ID.
     * 
     * @param badgeId The badge ID to search for
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByBadgeId(String badgeId);
    
    /**
     * Find all non-deleted employees by status with pagination support.
     * 
     * @param status The employment status to filter by
     * @param pageable Pagination information
     * @return Page of employees matching the criteria
     */
    Page<Employee> findAllByStatusAndDeletedFalse(String status, Pageable pageable);
    
    /**
     * Find all non-deleted employees by department.
     * 
     * @param department The department to filter by
     * @return List of employees in the specified department
     */
    List<Employee> findAllByDepartmentAndDeletedFalse(String department);
    
    /**
     * Find all non-deleted employees by role.
     * 
     * @param role The role to filter by
     * @return List of employees with the specified role
     */
    List<Employee> findAllByRoleAndDeletedFalse(String role);
    
    /**
     * Find all non-deleted employees by shift group.
     * 
     * @param shiftGroup The shift group to filter by
     * @return List of employees in the specified shift group
     */
    List<Employee> findAllByShiftGroupAndDeletedFalse(String shiftGroup);
    
    /**
     * Check if an employee with the given badge ID exists (excluding deleted records).
     * 
     * @param badgeId The badge ID to check
     * @return true if an employee with this badge ID exists and is not deleted
     */
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
    
    /**
     * Find all non-deleted employees with pagination.
     * 
     * @param pageable Pagination information
     * @return Page of non-deleted employees
     */
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    /**
     * Custom query to find employees by multiple criteria.
     * 
     * @param department Optional department filter
     * @param role Optional role filter
     * @param status Optional status filter
     * @param pageable Pagination information
     * @return Page of employees matching the criteria
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:role IS NULL OR e.role = :role) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "e.deleted = false")
    Page<Employee> findByMultipleCriteria(
        @Param("department") String department,
        @Param("role") String role,
        @Param("status") String status,
        Pageable pageable
    );
    
    /**
     * Count non-deleted employees by department.
     * 
     * @param department The department to count employees for
     * @return Number of employees in the department
     */
    long countByDepartmentAndDeletedFalse(String department);
    
    /**
     * Count non-deleted employees by status.
     * 
     * @param status The status to count employees for
     * @return Number of employees with the specified status
     */
    long countByStatusAndDeletedFalse(String status);
}