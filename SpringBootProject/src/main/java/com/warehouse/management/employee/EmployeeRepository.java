package com.warehouse.management.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailAndDeletedFalse(String email);
    Optional<Employee> findByEmployeeCodeAndDeletedFalse(String employeeCode);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
