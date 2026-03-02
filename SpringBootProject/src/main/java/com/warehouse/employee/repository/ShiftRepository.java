package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Repository interface for Shift entity.
 */
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    /**
     * Find shifts by department.
     */
    List<Shift> findByDepartment(String department);

    /**
     * Find shifts by recurring status.
     */
    List<Shift> findByIsRecurring(Boolean isRecurring);

    /**
     * Find shifts containing a specific day of week.
     */
    @Query("SELECT s FROM Shift s JOIN s.daysOfWeek d WHERE d = :dayOfWeek")
    List<Shift> findByDaysOfWeekContaining(@Param("dayOfWeek") DayOfWeek dayOfWeek);
}
