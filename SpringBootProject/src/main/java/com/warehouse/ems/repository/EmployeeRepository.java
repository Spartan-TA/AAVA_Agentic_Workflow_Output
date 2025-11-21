package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByBadgeId(String badgeId);
}