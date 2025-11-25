package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Employee entity with custom query methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByTenantId(Long tenantId);
}
