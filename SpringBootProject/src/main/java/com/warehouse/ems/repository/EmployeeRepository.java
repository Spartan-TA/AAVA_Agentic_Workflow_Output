package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity CRUD and custom queries.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findBySoftDeletedFalse();
    @Query("SELECT e FROM Employee e WHERE e.softDeleted = false AND (:department IS NULL OR e.department = :department)")
    List<Employee> findActiveByDepartment(@Param("department") String department);
}
