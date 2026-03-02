package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Find employee by badgeId.
     */
    Optional<Employee> findByBadgeId(String badgeId);

    /**
     * Find employees by status.
     */
    List<Employee> findByStatus(Employee.Status status);

    /**
     * Find employees by department.
     */
    List<Employee> findByDepartment(String department);

    /**
     * Find employees by role.
     */
    List<Employee> findByRole(Employee.Role role);

    /**
     * Check if an employee exists by badgeId.
     */
    boolean existsByBadgeId(String badgeId);

    /**
     * Complex search with pagination and filtering by department, status, and role.
     */
    @Query("SELECT e FROM Employee e WHERE (:department IS NULL OR e.department = :department) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:role IS NULL OR e.role = :role)")
    Page<Employee> searchEmployees(@Param("department") String department,
                                   @Param("status") Employee.Status status,
                                   @Param("role") Employee.Role role,
                                   Pageable pageable);
}
