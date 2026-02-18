package com.companyname.wems.employee.repository;

import com.companyname.wems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}