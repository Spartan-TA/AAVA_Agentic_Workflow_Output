package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity CRUD operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByDeletedFalse();
}
