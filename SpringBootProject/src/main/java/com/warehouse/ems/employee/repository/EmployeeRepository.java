package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity with CRUD, pagination, and filtering.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);

    @Query("SELECT e FROM Employee e WHERE (:name IS NULL OR e.name LIKE %:name%) AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role) AND e.deleted = false")
    Page<Employee> filterEmployees(String name, String department, String role, Pageable pageable);
}
