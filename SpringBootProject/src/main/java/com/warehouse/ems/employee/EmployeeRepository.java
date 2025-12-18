package com.warehouse.ems.employee;

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
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:name IS NULL OR e.name LIKE %:name%) AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role)")
    Page<Employee> filterEmployees(@Param("name") String name, @Param("department") String department, @Param("role") String role, Pageable pageable);

    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
