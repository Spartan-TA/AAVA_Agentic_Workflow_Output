package com.wms.ems.repository;

import com.wms.ems.entity.Employee;
import com.wms.ems.enums.EmployeeRole;
import com.wms.ems.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity operations.
 * Provides CRUD and custom query methods for Employee management.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Find an employee by their badge ID.
     * @param badgeId the badge ID
     * @return Optional of Employee
     */
    Optional<Employee> findByBadgeId(String badgeId);

    /**
     * Find employees by department.
     * @param department the department name
     * @return List of Employees
     */
    List<Employee> findByDepartment(String department);

    /**
     * Find employees by status.
     * @param status the employee status
     * @return List of Employees
     */
    List<Employee> findByStatus(EmployeeStatus status);

    /**
     * Find all non-deleted employees with pagination.
     * @param pageable pagination info
     * @return Page of Employees
     */
    Page<Employee> findByDeletedFalse(Pageable pageable);

    /**
     * Find all active employees by role.
     * @param role the employee role
     * @return List of Employees
     */
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.role = :role")
    List<Employee> findActiveByRole(@Param("role") EmployeeRole role);
}
