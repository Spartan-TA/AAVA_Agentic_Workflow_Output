package com.example.warehouse.repository;

import com.example.warehouse.entity.Attendance;
import com.example.warehouse.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);

    @Query("SELECT SUM(a.hoursWorked) FROM Attendance a WHERE a.employee = :employee AND a.date BETWEEN :start AND :end")
    Double calculateHoursWorked(@Param("employee") Employee employee, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
