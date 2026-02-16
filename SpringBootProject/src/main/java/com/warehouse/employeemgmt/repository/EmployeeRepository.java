package com.warehouse.employeemgmt.repository;

import com.warehouse.employeemgmt.domain.Employee;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * EmployeeRepository - Data access layer for Employee entity
 * 
 * Provides CRUD operations and custom queries for employee management.
 * Supports filtering, pagination, and soft-delete operations.
 * 
 * Features:
 * - Badge ID lookup with soft-delete filter
 * - Department and role filtering
 * - Active employee queries
 * - Custom search capabilities
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    
    /**
     * Find employee by badge ID excluding soft-deleted records
     */
    Optional<Employee> findByBadgeIdAndSoftDeletedFalse(String badgeId);
    
    /**
     * Check if badge ID exists
     */
    boolean existsByBadgeId(String badgeId);
    
    /**
     * Find all active employees (not soft-deleted)
     */
    List<Employee> findAllBySoftDeletedFalse();
    
    /**
     * Find employees by department
     */
    List<Employee> findByDepartmentAndSoftDeletedFalse(String department);
    
    /**
     * Find employees by role
     */
    List<Employee> findByRoleAndSoftDeletedFalse(String role);
    
    /**
     * Find employees by status
     */
    List<Employee> findByStatusAndSoftDeletedFalse(String status);
    
    /**
     * Custom query to find employees by name pattern
     */
    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.softDeleted = false")
    List<Employee> searchByName(@Param("name") String name);
    
    /**
     * Find employees by shift group
     */
    List<Employee> findByShiftGroupAndSoftDeletedFalse(String shiftGroup);
}