package com.company.wms.employee.repository;

import com.company.wms.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    List<Employee> findByDepartmentAndDeletedFalse(String department);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.name LIKE %:name%")
    List<Employee> searchByName(@Param("name") String name);
}
