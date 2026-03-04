package com.warehouse.ems.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.employee.id = :employeeId AND s.date = :date")
    List<Schedule> findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.date = :date AND s.status = 'CONFLICT'")
    List<Schedule> findConflictsByDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.shift.id = :shiftId AND s.date = :date")
    List<Schedule> findByShiftAndDate(@Param("shiftId") Long shiftId, @Param("date") LocalDate date);

    // Bulk assignment: find all schedules for a given shift template
    @Query("SELECT s FROM Schedule s WHERE s.shift.id = :shiftId")
    List<Schedule> findByShift(@Param("shiftId") Long shiftId);
}
