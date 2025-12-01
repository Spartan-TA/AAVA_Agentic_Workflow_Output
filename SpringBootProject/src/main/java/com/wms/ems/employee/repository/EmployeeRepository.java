package com.wms.ems.employee.repository;

import com.wms.ems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom queries for Employee management.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Find an employee by their badge ID.
     * @param badgeId the badge ID
     * @return Optional of Employee
     */
    Optional<Employee> findByBadgeId(String badgeId);

    /**
     * Find all employees that are not marked as deleted (soft delete).
     * @return List of active employees
     */
    List<Employee> findAllByDeletedFalse();

    /**
     * Custom query to find employees by department.
     * @param department the department name
     * @return List of employees in the department
     */
    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.deleted = false")
    List<Employee> findActiveByDepartment(String department);
}
