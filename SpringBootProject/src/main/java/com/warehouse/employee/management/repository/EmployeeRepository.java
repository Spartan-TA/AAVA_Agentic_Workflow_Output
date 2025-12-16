package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity with soft-delete and filtering support.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:name IS NULL OR e.name LIKE %:name%) AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role)")
    Page<Employee> filterEmployees(String name, String department, String role, Pageable pageable);
}
