package com.company.warehouse.employee.repository;

import com.company.warehouse.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
