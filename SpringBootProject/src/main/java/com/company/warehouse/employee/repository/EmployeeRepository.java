package com.company.warehouse.employee.repository;

import com.company.warehouse.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity CRUD operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);

    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId")
    java.util.List<Employee> findAllByTenantId(@Param("tenantId") String tenantId);
}
