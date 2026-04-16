package com.warehouse.ems.repository;

import com.warehouse.ems.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository for Employee entity with badgeId uniqueness and soft-delete support.
 */
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    boolean existsByBadgeIdAndStatusNot(String badgeId, Employee.Status status);
    Page<Employee> findAllByStatus(Employee.Status status, Pageable pageable);
}
