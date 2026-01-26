package com.company.warehouse.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByDepartment(String department);
    List<Employee> findByStatus(String status);

    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.status = :status")
    Page<Employee> findByDepartmentAndStatus(@Param("department") String department, @Param("status") String status, Pageable pageable);
}
