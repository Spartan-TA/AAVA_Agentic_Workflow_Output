package com.company.wems.repository;

import com.company.wems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Optional<Employee> findByEmailAndDeletedFalse(String email);
    List<Employee> findAllByTenantIdAndDeletedFalse(Long tenantId);
}
