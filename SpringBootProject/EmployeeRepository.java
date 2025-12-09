package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for Employee entity.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByLastName(String lastName);
    List<Employee> findByActiveTrue();
    @Query("SELECT e FROM Employee e WHERE e.certifications IS NOT EMPTY")
    List<Employee> findCertifiedEmployees();
    @Query("SELECT e FROM Employee e WHERE e.id IN :ids")
    List<Employee> findByIds(@Param("ids") List<Long> ids);
}
