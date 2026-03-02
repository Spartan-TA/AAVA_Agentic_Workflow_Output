package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Employee entities.
 */
public interface EmployeeService {
    /**
     * Create a new employee.
     * @param employee Employee entity
     * @return Created Employee
     */
    Employee createEmployee(Employee employee);

    /**
     * Update an existing employee.
     * @param employee Employee entity
     * @return Updated Employee
     */
    Employee updateEmployee(Employee employee);

    /**
     * Get employee by ID.
     * @param id Employee ID
     * @return Optional Employee
     */
    Optional<Employee> getEmployeeById(Long id);

    /**
     * Get all employees with pagination.
     * @param pageable Pageable
     * @return Page of Employees
     */
    Page<Employee> getAllEmployees(Pageable pageable);

    /**
     * Soft delete employee by ID.
     * @param id Employee ID
     */
    void deleteEmployee(Long id);

    /**
     * Find employee by badge ID.
     * @param badgeId Badge ID
     * @return Optional Employee
     */
    Optional<Employee> findByBadgeId(String badgeId);

    /**
     * Find employees by department.
     * @param department Department name
     * @return List of Employees
     */
    List<Employee> findByDepartment(String department);

    /**
     * Find employees by role.
     * @param role Role name
     * @return List of Employees
     */
    List<Employee> findByRole(String role);

    /**
     * Search employees by keyword (name, badgeId, etc).
     * @param keyword Search keyword
     * @param pageable Pageable
     * @return Page of Employees
     */
    Page<Employee> searchEmployees(String keyword, Pageable pageable);
}
