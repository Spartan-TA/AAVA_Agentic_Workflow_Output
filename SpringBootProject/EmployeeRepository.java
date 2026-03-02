package com.wms.employee;

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
 * Key Features:
 * - Badge ID uniqueness validation
 * - Soft delete filtering
 * - Multi-criteria filtering (department, role, status)
 * - Pagination support
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Check if an employee with the given badge ID exists.
     * Used for uniqueness validation during employee creation.
     * 
     * @param badgeId The badge ID to check
     * @return true if badge ID exists, false otherwise
     */
    boolean existsByBadgeId(String badgeId);
    
    /**
     * Find an employee by badge ID.
     * 
     * @param badgeId The badge ID to search for
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByBadgeId(String badgeId);
    
    /**
     * Find all active (non-deleted) employees.
     * 
     * @param pageable Pagination parameters
     * @return Page of active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);
    
    /**
     * Find employees by status, excluding soft-deleted records.
     * 
     * @param status The employee status to filter by (can be null for all statuses)
     * @param pageable Pagination parameters
     * @return Page of employees matching the criteria
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> findByStatus(@Param("status") EmployeeStatus status, Pageable pageable);
    
    /**
     * Find employees by multiple filter criteria.
     * All parameters are optional (null means no filter applied).
     * 
     * @param department Department to filter by
     * @param role Role to filter by
     * @param status Status to filter by
     * @param pageable Pagination parameters
     * @return Page of employees matching all non-null criteria
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:department IS NULL OR e.department = :department) " +
           "AND (:role IS NULL OR e.role = :role) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> findByFilters(
        @Param("department") String department,
        @Param("role") String role,
        @Param("status") EmployeeStatus status,
        Pageable pageable
    );
    
    /**
     * Find all employees in a specific department.
     * 
     * @param department The department name
     * @return List of employees in the department
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.department = :department")
    List<Employee> findByDepartment(@Param("department") String department);
    
    /**
     * Count active employees by status.
     * 
     * @param status The status to count
     * @return Number of active employees with the given status
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.deleted = false AND e.status = :status")
    long countByStatus(@Param("status") EmployeeStatus status);
}
