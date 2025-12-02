package com.wms.ems.employee;

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
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:name IS NULL OR e.name ILIKE %:name%) AND (:departmentId IS NULL OR e.department.id = :departmentId)")
    Page<Employee> filter(@Param("name") String name, @Param("departmentId") Long departmentId, Pageable pageable);
}
