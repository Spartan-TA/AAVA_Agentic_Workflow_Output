package com.warehouse.employee.repository;

import com.warehouse.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);

    @Query("SELECT e FROM Employee e WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:department IS NULL OR e.department = :department) " +
           "AND (:role IS NULL OR e.role = :role) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> filterEmployees(String name, String department, String role, String status, Pageable pageable);
}
