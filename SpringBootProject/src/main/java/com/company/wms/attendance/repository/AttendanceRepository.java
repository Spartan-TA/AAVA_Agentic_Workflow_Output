package com.company.wms.attendance.repository;

import com.company.wms.attendance.model.AttendanceRecord;
import com.company.wms.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for AttendanceRecord entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployee(Employee employee);
    List<AttendanceRecord> findByDate(LocalDate date);
}
