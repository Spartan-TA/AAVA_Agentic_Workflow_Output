package com.companyname.wems.employee.repository;

import com.companyname.wems.employee.entity.Employee;
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
 * Repository interface for Employee entity
 * 
 * Provides CRUD operations and custom query methods for employee management.
 * Extends JpaRepository for standard database operations and
 * JpaSpecificationExecutor for dynamic query support.
 * 
 * Key features:
 * - Badge ID lookup for clock-in/out operations
 * - Department and status filtering
 * - Pagination support for large datasets
 * - Custom queries for reporting
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, 
                                            JpaSpecificationExecutor<Employee> {
    
    /**
     * Find employee by unique badge ID
     * Used for authentication and clock-in/out operations
     * 
     * @param badgeId Unique badge identifier
     * @return Optional containing employee if found
     */
    Optional<Employee> findByBadgeId(String badgeId);
    
    /**
     * Check if employee exists with given badge ID
     * Used for validation during employee creation
     * 
     * @param badgeId Badge ID to check
     * @return true if badge ID exists, false otherwise
     */
    boolean existsByBadgeId(String badgeId);
    
    /**
     * Find all employees by status with pagination
     * Used for filtering active/inactive employees
     * 
     * @param status Employee status to filter by
     * @param pageable Pagination parameters
     * @return Page of employees matching status
     */
    Page<Employee> findByStatus(Employee.Status status, Pageable pageable);
    
    /**
     * Find all employees by department with pagination
     * Used for department-specific reporting
     * 
     * @param department Department name
     * @param pageable Pagination parameters
     * @return Page of employees in department
     */
    Page<Employee> findByDepartment(String department, Pageable pageable);
    
    /**
     * Find employees by department and status
     * Used for filtered employee lists
     * 
     * @param department Department name
     * @param status Employee status
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    Page<Employee> findByDepartmentAndStatus(String department, 
                                             Employee.Status status, 
                                             Pageable pageable);
    
    /**
     * Find employees by role
     * Used for role-based reporting and management
     * 
     * @param role Employee role
     * @param pageable Pagination parameters
     * @return Page of employees with specified role
     */
    Page<Employee> findByRole(Employee.Role role, Pageable pageable);
    
    /**
     * Find employees by shift group
     * Used for shift scheduling and management
     * 
     * @param shiftGroup Shift group identifier
     * @param pageable Pagination parameters
     * @return Page of employees in shift group
     */
    Page<Employee> findByShiftGroup(String shiftGroup, Pageable pageable);
    
    /**
     * Count employees by department
     * Used for staffing analytics
     * 
     * @param department Department name
     * @return Count of employees in department
     */
    long countByDepartment(String department);
    
    /**
     * Count employees by status
     * Used for workforce analytics
     * 
     * @param status Employee status
     * @return Count of employees with status
     */
    long countByStatus(Employee.Status status);
    
    /**
     * Find all active employees in a department
     * Custom query for common use case
     * 
     * @param department Department name
     * @return List of active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.status = 'ACTIVE'")
    List<Employee> findActiveEmployeesByDepartment(@Param("department") String department);
    
    /**
     * Find employees by name pattern (case-insensitive)
     * Used for employee search functionality
     * 
     * @param namePattern Name pattern to search
     * @param pageable Pagination parameters
     * @return Page of matching employees
     */
    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    Page<Employee> searchByName(@Param("namePattern") String namePattern, Pageable pageable);
    
    /**
     * Find employees hired within a date range
     * Used for onboarding reports
     * 
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of employees hired in range
     */
    @Query("SELECT e FROM Employee e WHERE e.hireDate BETWEEN :startDate AND :endDate")
    List<Employee> findByHireDateBetween(@Param("startDate") java.time.LocalDate startDate, 
                                         @Param("endDate") java.time.LocalDate endDate);
}