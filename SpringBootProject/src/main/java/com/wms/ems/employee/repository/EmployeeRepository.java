package com.wms.ems.employee.repository;

import com.wms.ems.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity operations.
 * Provides CRUD operations and custom queries for Employee management.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds an employee by their badge ID.
     * @param badgeId the badge ID
     * @return an Optional containing the Employee if found
     */
    Optional<Employee> findByBadgeId(String badgeId);

    /**
     * Finds all non-deleted employees with pagination.
     * @param pageable pagination information
     * @return a page of non-deleted employees
     */
    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    /**
     * Finds all non-deleted employees in a specific department.
     * @param department the department name
     * @return a list of non-deleted employees in the department
     */
    List<Employee> findByDepartmentAndDeletedFalse(String department);
}
