package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Leave entity.
 */
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    /**
     * Find leaves for an employee by status.
     */
    List<Leave> findByEmployeeAndStatus(Employee employee, Leave.Status status);

    /**
     * Find leaves for an employee between two start dates.
     */
    List<Leave> findByEmployeeAndStartDateBetween(Employee employee, LocalDate start, LocalDate end);

    /**
     * Calculate remaining leave balance for an employee.
     */
    @Query("SELECT SUM(l.balance) FROM Leave l WHERE l.employee = :employee")
    Double calculateRemainingBalance(@Param("employee") Employee employee);
}
