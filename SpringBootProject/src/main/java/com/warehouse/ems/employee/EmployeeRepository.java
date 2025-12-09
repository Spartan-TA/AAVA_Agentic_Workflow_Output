package com.warehouse.ems.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity with custom queries for badgeId and soft delete.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    List<Employee> findAllByDeletedFalse();
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role)")
    List<Employee> filterByDepartmentAndRole(@Param("department") String department, @Param("role") String role);
}
