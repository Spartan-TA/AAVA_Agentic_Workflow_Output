package com.wms.core.repository;

import com.wms.core.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByBadgeIdAndSoftDeletedFalse(String badgeId);
    boolean existsByBadgeId(String badgeId);
}