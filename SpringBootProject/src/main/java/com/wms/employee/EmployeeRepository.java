package com.wms.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);
    Page<Employee> findByStatusAndDeletedFalse(String status, Pageable pageable);
}
