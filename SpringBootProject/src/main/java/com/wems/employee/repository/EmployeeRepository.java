package com.wems.employee.repository;

import com.wems.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for Employee entity.
 * Supports pagination and filtering by active status (soft delete).
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds all active employees with pagination.
     * @param pageable Pageable object
     * @return Page of active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true")
    Page<Employee> findAllActive(Pageable pageable);

    /**
     * Finds employee by badgeId if active.
     * @param badgeId Unique badge identifier
     * @return Employee or null
     */
    Employee findByBadgeIdAndActiveTrue(String badgeId);
}
