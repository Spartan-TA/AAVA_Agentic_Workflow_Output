package com.warehouse.employee.repository;

import com.warehouse.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity operations.
 * Provides custom query methods for soft delete pattern and advanced filtering.
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find an employee by badge ID, excluding soft-deleted records.
     * 
     * @param badgeId The unique badge identifier
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    /**
     * Find all non-deleted employees with pagination support.
     * 
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    /**
     * Find employees by department, excluding soft-deleted records.
     * 
     * @param department Department name
     * @param pageable Pagination parameters
     * @return Page of employees in the specified department
     */
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);

    /**
     * Find employees by role, excluding soft-deleted records.
     * 
     * @param role Employee role
     * @param pageable Pagination parameters
     * @return Page of employees with the specified role
     */
    Page<Employee> findByRoleAndDeletedFalse(String role, Pageable pageable);

    /**
     * Find employees by status, excluding soft-deleted records.
     * 
     * @param status Employment status
     * @param pageable Pagination parameters
     * @return Page of employees with the specified status
     */
    Page<Employee> findByStatusAndDeletedFalse(String status, Pageable pageable);

    /**
     * Search employees by name containing the search term, excluding soft-deleted records.
     * 
     * @param name Search term for employee name
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    Page<Employee> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

    /**
     * Advanced search with multiple filters.
     * 
     * @param name Name filter (optional)
     * @param department Department filter (optional)
     * @param role Role filter (optional)
     * @param status Status filter (optional)
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:role IS NULL OR e.role = :role) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "e.deleted = false")
    Page<Employee> searchEmployees(
        @Param("name") String name,
        @Param("department") String department,
        @Param("role") String role,
        @Param("status") String status,
        Pageable pageable
    );

    /**
     * Count active (non-deleted) employees.
     * 
     * @return Count of active employees
     */
    long countByDeletedFalse();

    /**
     * Count employees by department.
     * 
     * @param department Department name
     * @return Count of employees in the department
     */
    long countByDepartmentAndDeletedFalse(String department);

    /**
     * Check if badge ID exists (excluding soft-deleted records).
     * 
     * @param badgeId Badge identifier to check
     * @return true if badge ID exists
     */
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);

    /**
     * Find all employees in a specific shift group.
     * 
     * @param shiftGroup Shift group identifier
     * @return List of employees in the shift group
     */
    List<Employee> findByShiftGroupAndDeletedFalse(String shiftGroup);
}