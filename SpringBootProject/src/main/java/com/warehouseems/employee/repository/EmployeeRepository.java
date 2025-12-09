package com.warehouseems.employee.repository;

import com.warehouseems.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * Repository for Employee entity with soft-delete and filtering support.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Find by badgeId (excluding soft-deleted)
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    // Find all non-deleted employees with pagination
    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    // Filtering by department, role, and status (example)
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role) AND (:status IS NULL OR e.status = :status)")
    Page<Employee> filterEmployees(@Param("department") String department, @Param("role") String role, @Param("status") String status, Pageable pageable);
}
