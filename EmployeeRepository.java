package com.wms.ems.employee.repository;

import com.wms.ems.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByDepartment(String department);
    List<Employee> findByRole(String role);
    List<Employee> findByShiftGroup(String shiftGroup);
    List<Employee> findByStatus(String status);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    List<Employee> findAllActive();
}
