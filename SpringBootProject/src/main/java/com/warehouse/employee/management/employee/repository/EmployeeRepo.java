package com.warehouse.employee.management.employee.repository;

import com.warehouse.employee.management.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    // Custom query methods if needed
}
