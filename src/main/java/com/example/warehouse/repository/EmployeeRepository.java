package com.example.warehouse.repository;

import com.example.warehouse.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByBadgeId(String badgeId);
    Page<Employee> findByDepartment(String department, Pageable pageable);
    Page<Employee> findByStatus(String status, Pageable pageable);
}
