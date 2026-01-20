package com.company.warehouse.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * Repository for Employee entity.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role)")
    Page<Employee> filter(@Param("department") String department, @Param("role") String role, Pageable pageable);
}
