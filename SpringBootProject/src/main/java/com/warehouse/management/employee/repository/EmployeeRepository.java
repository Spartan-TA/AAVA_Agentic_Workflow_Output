package com.warehouse.management.employee.repository;

import com.warehouse.management.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("select e from Employee e where e.deleted = false and (lower(e.name) like lower(concat('%', ?1, '%')) or lower(e.department) like lower(concat('%', ?1, '%')))")
    Page<Employee> search(String keyword, Pageable pageable);
}
