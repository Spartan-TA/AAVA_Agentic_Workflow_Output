package com.warehouse.employee.management.employee.repository;

import com.warehouse.employee.management.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    boolean existsByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:departmentId IS NULL OR e.department.id = :departmentId)")
    java.util.List<Employee> findAllActive(@Param("departmentId") Long departmentId);
}
