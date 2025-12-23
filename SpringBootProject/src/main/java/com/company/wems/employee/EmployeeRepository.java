package com.company.wems.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Employee entity.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    List<Employee> findAllByDeletedFalse();
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:role IS NULL OR e.role = :role) AND (:department IS NULL OR e.department = :department)")
    List<Employee> filter(@Param("role") String role, @Param("department") String department);
}
