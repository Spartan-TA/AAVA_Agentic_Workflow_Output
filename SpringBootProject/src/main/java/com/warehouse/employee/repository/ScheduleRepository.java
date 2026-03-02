package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.Schedule;
import com.warehouse.employee.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Schedule entity.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    /**
     * Find schedules for an employee between two dates.
     */
    List<Schedule> findByEmployeeAndScheduleDateBetween(Employee employee, LocalDate start, LocalDate end);

    /**
     * Find schedules by shift and schedule date.
     */
    List<Schedule> findByShiftAndScheduleDate(Shift shift, LocalDate scheduleDate);

    /**
     * Find conflicting schedules for an employee on a given date.
     */
    @Query("SELECT s FROM Schedule s WHERE s.employee = :employee AND s.scheduleDate = :scheduleDate")
    List<Schedule> findConflicts(@Param("employee") Employee employee, @Param("scheduleDate") LocalDate scheduleDate);
}
