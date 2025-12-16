package com.companyname.wems.attendance.service;

import com.companyname.wems.attendance.model.AttendanceEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceService for Time & Attendance (E04)
 * Implements clock-in/out, shift calculation, missed punch correction
 */
@Service
public class AttendanceService {
    // In a real implementation, inject AttendanceEventRepository

    private final List<AttendanceEvent> events = new ArrayList<>(); // Simulated DB

    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String location, String deviceInfo) {
        AttendanceEvent event = new AttendanceEvent(null, employeeId, "CLOCK_IN", LocalDateTime.now(), location, deviceInfo);
        events.add(event);
        return event;
    }

    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String location, String deviceInfo) {
        AttendanceEvent event = new AttendanceEvent(null, employeeId, "CLOCK_OUT", LocalDateTime.now(), location, deviceInfo);
        events.add(event);
        return event;
    }

    public List<AttendanceEvent> getEventsForEmployee(Long employeeId) {
        List<AttendanceEvent> result = new ArrayList<>();
        for (AttendanceEvent e : events) {
            if (e.getEmployeeId().equals(employeeId)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Calculate total shift hours for an employee
     */
    public double calculateShiftHours(Long employeeId) {
        List<AttendanceEvent> empEvents = getEventsForEmployee(employeeId);
        double totalHours = 0.0;
        LocalDateTime lastIn = null;
        for (AttendanceEvent e : empEvents) {
            if ("CLOCK_IN".equals(e.getEventType())) {
                lastIn = e.getTimestamp();
            } else if ("CLOCK_OUT".equals(e.getEventType()) && lastIn != null) {
                totalHours += Duration.between(lastIn, e.getTimestamp()).toMinutes() / 60.0;
                lastIn = null;
            }
        }
        return totalHours;
    }

    /**
     * Handle missed punch correction (manual event addition)
     */
    @Transactional
    public AttendanceEvent addCorrectionEvent(Long employeeId, String eventType, LocalDateTime timestamp, String location, String deviceInfo) {
        AttendanceEvent event = new AttendanceEvent(null, employeeId, eventType, timestamp, location, deviceInfo);
        events.add(event);
        return event;
    }
}
