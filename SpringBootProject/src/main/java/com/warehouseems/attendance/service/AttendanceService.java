package com.warehouseems.attendance.service;

import com.warehouseems.attendance.model.AttendanceEvent;
import com.warehouseems.attendance.repository.AttendanceEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling attendance business logic: clock-in/out, corrections, and hours calculation.
 */
@Service
public class AttendanceService {
    @Autowired
    private AttendanceEventRepository attendanceEventRepository;

    /**
     * Clock in for an employee.
     */
    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String location) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.EventType.IN);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        return attendanceEventRepository.save(event);
    }

    /**
     * Clock out for an employee.
     */
    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String deviceId, String location) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.EventType.OUT);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        return attendanceEventRepository.save(event);
    }

    /**
     * Correction logic: create a correction event (could be extended for approval workflow).
     */
    @Transactional
    public AttendanceEvent correctEvent(Long eventId, LocalDateTime correctedTimestamp) {
        AttendanceEvent event = attendanceEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance event not found"));
        event.setTimestamp(correctedTimestamp);
        return attendanceEventRepository.save(event);
    }

    /**
     * Calculate total hours worked for an employee on a given day.
     */
    public double calculateHours(Long employeeId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<AttendanceEvent> events = attendanceEventRepository.findByEmployeeIdAndTimestampBetween(employeeId, start, end);
        events.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));
        double totalHours = 0.0;
        LocalDateTime lastIn = null;
        for (AttendanceEvent event : events) {
            if (event.getType() == AttendanceEvent.EventType.IN) {
                lastIn = event.getTimestamp();
            } else if (event.getType() == AttendanceEvent.EventType.OUT && lastIn != null) {
                totalHours += Duration.between(lastIn, event.getTimestamp()).toMinutes() / 60.0;
                lastIn = null;
            }
        }
        return totalHours;
    }
}