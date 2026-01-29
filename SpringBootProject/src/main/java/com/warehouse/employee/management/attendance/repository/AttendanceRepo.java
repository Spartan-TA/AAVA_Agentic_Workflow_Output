package com.warehouse.employee.management.attendance.repository;

import com.warehouse.employee.management.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    // Custom query methods if needed
}
