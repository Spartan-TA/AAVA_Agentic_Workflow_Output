package com.wms.attendance.service;

import com.wms.attendance.entity.AttendanceEvent;
import com.wms.attendance.repository.AttendanceEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling attendance logic such as clock-in, clock-out, and daily totals.
 */
@Service
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;

    @Autowired
    public AttendanceService(AttendanceEventRepository attendanceEventRepository) {
        this.attendanceEventRepository = attendanceEventRepository;
    }

    /**
     * Clock in for an employee.
     * @param employeeId Employee ID
     * @param location Location (optional)
     * @return AttendanceEvent
     */
    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String location) {
        AttendanceEvent event = new AttendanceEvent(employeeId, LocalDateTime.now(), AttendanceEvent.EventType.CLOCK_IN, location);
        return attendanceEventRepository.save(event);
    }

    /**
     * Clock out for an employee.
     * @param employeeId Employee ID
     * @param location Location (optional)
     * @return AttendanceEvent
     */
    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String location) {
        AttendanceEvent event = new AttendanceEvent(employeeId, LocalDateTime.now(), AttendanceEvent.EventType.CLOCK_OUT, location);
        return attendanceEventRepository.save(event);
    }

    /**
     * Calculate total hours worked for an employee on a given day.
     * @param employeeId Employee ID
     * @param date Date
     * @return Total hours worked
     */
    public double calculateDailyTotals(Long employeeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<AttendanceEvent> events = attendanceEventRepository.findByEmployeeIdAndEventTimeBetween(employeeId, startOfDay, endOfDay);
        events.sort((e1, e2) -> e1.getEventTime().compareTo(e2.getEventTime()));
        double totalHours = 0.0;
        LocalDateTime lastClockIn = null;
        for (AttendanceEvent event : events) {
            if (event.getEventType() == AttendanceEvent.EventType.CLOCK_IN) {
                lastClockIn = event.getEventTime();
            } else if (event.getEventType() == AttendanceEvent.EventType.CLOCK_OUT && lastClockIn != null) {
                totalHours += java.time.Duration.between(lastClockIn, event.getEventTime()).toMinutes() / 60.0;
                lastClockIn = null;
            }
        }
        return totalHours;
    }
}
