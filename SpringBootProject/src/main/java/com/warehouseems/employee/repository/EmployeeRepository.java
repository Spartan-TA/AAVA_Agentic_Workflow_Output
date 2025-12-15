package com.warehouseems.employee.repository;

import com.warehouseems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for Employee entity.
 * Extends JpaRepository for CRUD and JpaSpecificationExecutor for filtering.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByBadgeId(String badgeId);
}
