package com.companyname.wem.attendance.repository;

import com.companyname.wem.attendance.domain.AttendanceEvent;
import com.companyname.wem.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployee(Employee employee);
    List<AttendanceEvent> findByEmployeeAndTimestampBetween(Employee employee, LocalDateTime start, LocalDateTime end);
    List<AttendanceEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
