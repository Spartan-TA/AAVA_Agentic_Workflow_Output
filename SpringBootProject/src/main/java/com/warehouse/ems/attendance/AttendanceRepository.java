package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND DATE(a.clockIn) = :date")
    List<Attendance> findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.clockOut IS NULL AND a.clockIn < CURRENT_DATE")
    List<Attendance> findMissedPunches();

    @Query("SELECT a.employee.id, SUM(a.hoursWorked) FROM Attendance a WHERE DATE(a.clockIn) = :date GROUP BY a.employee.id")
    List<Object[]> findDailyTotals(@Param("date") LocalDate date);
}
