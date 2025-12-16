package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

/**
 * Repository for Employee entity with CRUD and filtering support.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
