package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity CRUD and custom queries.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:department IS NULL OR e.department = :department)")
    List<Employee> findAllActiveByDepartment(@Param("department") String department);
}
