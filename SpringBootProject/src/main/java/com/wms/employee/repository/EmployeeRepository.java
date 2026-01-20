package com.wms.employee.repository;

import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom queries for Employee management.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Custom query methods can be defined here if needed
}
