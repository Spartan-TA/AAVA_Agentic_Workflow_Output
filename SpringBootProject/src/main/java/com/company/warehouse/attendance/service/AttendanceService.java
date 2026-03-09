package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.repository.AttendanceRepository;
import com.company.warehouse.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Attendance business logic.
 */
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * Clock in or out for an employee.
     */
    @Transactional
    public AttendanceEvent recordEvent(@Valid @NotNull AttendanceEvent event) {
        // Additional validation logic can be added here
        return attendanceRepository.save(event);
    }

    /**
     * Get paginated attendance events for an employee.
     */
    @Transactional(readOnly = true)
    public Page<AttendanceEvent> getEventsForEmployee(Long employeeId, Pageable pageable) {
        return attendanceRepository.findByEmployeeId(employeeId, pageable);
    }

    /**
     * Get attendance events for an employee in a date range.
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getEventsForEmployeeInRange(Long employeeId, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findEventsForEmployeeInRange(employeeId, start, end);
    }

    /**
     * Get attendance event by ID.
     */
    @Transactional(readOnly = true)
    public AttendanceEvent getEvent(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance event not found: " + id));
    }

    /**
     * Delete attendance event by ID.
     */
    @Transactional
    public void deleteEvent(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance event not found: " + id);
        }
        attendanceRepository.deleteById(id);
    }
}
