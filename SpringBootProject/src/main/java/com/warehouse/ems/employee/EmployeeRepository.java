package com.warehouse.ems.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * Spring Data JPA repository for Employee entity.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:name IS NULL OR e.name LIKE %:name%) AND (:department IS NULL OR e.department = :department)")
    Page<Employee> findAllActive(@Param("name") String name, @Param("department") String department, Pageable pageable);
}
