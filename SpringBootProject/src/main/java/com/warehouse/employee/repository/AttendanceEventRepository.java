package com.warehouse.employee.repository;

import com.warehouse.employee.domain.AttendanceEvent;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AttendanceEvent entity with custom query methods.
 */
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {

    /**
     * Find attendance events for an employee between two timestamps.
     * @param employee Employee
     * @param start Start time
     * @param end End time
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEmployeeAndEventTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);

    /**
     * Find the most recent attendance event for an employee.
     * @param employee Employee
     * @return Optional of AttendanceEvent
     */
    Optional<AttendanceEvent> findTopByEmployeeOrderByEventTimeDesc(Employee employee);
}
