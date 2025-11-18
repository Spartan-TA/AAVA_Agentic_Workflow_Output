package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity with custom queries for soft-delete and badgeId uniqueness.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:name IS NULL OR e.name LIKE %:name%) AND (:department IS NULL OR e.department = :department)")
    Page<Employee> filterEmployees(String name, String department, Pageable pageable);
}
