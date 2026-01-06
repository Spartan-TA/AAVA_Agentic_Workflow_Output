package com.companyname.warehouse.attendance.repository;

import com.companyname.warehouse.attendance.entity.Attendance;
import com.companyname.warehouse.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Attendance entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeAndClockInBetween(Employee employee, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByEmployee(Employee employee);
}
