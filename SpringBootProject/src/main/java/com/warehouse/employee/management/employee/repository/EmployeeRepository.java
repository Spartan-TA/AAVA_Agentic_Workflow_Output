package com.warehouse.employee.management.employee.repository;

import com.warehouse.employee.management.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity with custom query methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    Page<Employee> findAllByDepartmentAndDeletedFalse(String department, Pageable pageable);
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
