package com.example.warehouse.employee.repository;

import com.example.warehouse.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Find employee by email
    Optional<Employee> findByEmail(String email);
}
