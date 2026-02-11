package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Employee entity with custom query methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find employee by badge ID.
     * @param badgeId badge identifier
     * @return Optional of Employee
     */
    Optional<Employee> findByBadgeId(String badgeId);

    /**
     * Find all employees that are not soft-deleted.
     * @return List of Employee
     */
    List<Employee> findAllByDeletedFalse();

    /**
     * Check if an employee exists by badge ID and not deleted.
     * @param badgeId badge identifier
     * @return true if exists
     */
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
