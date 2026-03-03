package com.company.wms.employee.repository;

import com.company.wms.employee.entity.Employee;
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
 * Provides CRUD operations and custom queries with soft delete support.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find employee by badge ID excluding soft-deleted records
     * @param badgeId unique badge identifier
     * @return Optional containing employee if found
     */
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    /**
     * Find active employee by ID (not soft-deleted)
     * @param id employee ID
     * @return Optional containing employee if found and active
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.id = :id")
    Optional<Employee> findActiveById(@Param("id") Long id);

    /**
     * Find all active employees by department
     * @param department department name
     * @param pageable pagination information
     * @return Page of employees in the department
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.department = :department")
    Page<Employee> findByDepartmentAndDeletedFalse(@Param("department") String department, Pageable pageable);

    /**
     * Find all active employees by role
     * @param role employee role
     * @param pageable pagination information
     * @return Page of employees with the role
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.role = :role")
    Page<Employee> findByRoleAndDeletedFalse(@Param("role") String role, Pageable pageable);

    /**
     * Find all active employees by status
     * @param status employment status
     * @return List of employees with the status
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.status = :status")
    List<Employee> findByStatusAndDeletedFalse(@Param("status") String status);

    /**
     * Find all active employees by shift group
     * @param shiftGroup shift group identifier
     * @return List of employees in the shift group
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.shiftGroup = :shiftGroup")
    List<Employee> findByShiftGroupAndDeletedFalse(@Param("shiftGroup") String shiftGroup);

    /**
     * Count active employees by department
     * @param department department name
     * @return count of active employees
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.deleted = false AND e.department = :department")
    long countByDepartmentAndDeletedFalse(@Param("department") String department);

    /**
     * Find all active employees (not soft-deleted)
     * @param pageable pagination information
     * @return Page of active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);

    /**
     * Search employees by name (case-insensitive, partial match)
     * @param name search term
     * @param pageable pagination information
     * @return Page of matching employees
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Employee> searchByName(@Param("name") String name, Pageable pageable);
}