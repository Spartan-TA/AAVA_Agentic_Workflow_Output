package com.company.wms.attendance.service;

import com.company.wms.attendance.domain.AttendanceEvent;
import com.company.wms.attendance.repository.AttendanceRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing attendance events (clock-in/out).
 */
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * Clock-in event for employee.
     */
    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String notes) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setEventTime(LocalDateTime.now());
        event.setEventType("IN");
        event.setNotes(notes);
        return attendanceRepository.save(event);
    }

    /**
     * Clock-out event for employee.
     */
    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String notes) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setEventTime(LocalDateTime.now());
        event.setEventType("OUT");
        event.setNotes(notes);
        return attendanceRepository.save(event);
    }

    /**
     * Get all attendance events for an employee.
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getEventsForEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByEventTimeDesc(employeeId);
    }

    /**
     * Get attendance events for employee in date range.
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getEventsForEmployeeInRange(Long employeeId, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByEmployeeIdAndDateRange(employeeId, start, end);
    }

    /**
     * Get paginated attendance events for employee.
     */
    @Transactional(readOnly = true)
    public Page<AttendanceEvent> getEventsPage(Long employeeId, Pageable pageable) {
        return attendanceRepository.findAllByEmployeeId(employeeId, pageable);
    }

    /**
     * Get attendance event by ID.
     */
    @Transactional(readOnly = true)
    public AttendanceEvent getById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance event not found: " + id));
    }
}
