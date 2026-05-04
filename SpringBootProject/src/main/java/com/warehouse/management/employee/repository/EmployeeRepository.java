package com.warehouse.management.employee.repository;

import com.warehouse.management.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom queries.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Finds employees by department with pagination.
     * @param department Department name
     * @param pageable Pageable object
     * @return Page of employees
     */
    Page<Employee> findByDepartment(String department, Pageable pageable);

    /**
     * Checks if an employee exists by email.
     * @param email Employee email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}
