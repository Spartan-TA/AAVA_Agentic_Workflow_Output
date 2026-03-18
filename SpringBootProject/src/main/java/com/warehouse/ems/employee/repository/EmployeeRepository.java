package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom queries for Employee.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Find employee by email address.
     * @param email employee email
     * @return Optional of Employee
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Check if an employee exists by email.
     * @param email employee email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}
