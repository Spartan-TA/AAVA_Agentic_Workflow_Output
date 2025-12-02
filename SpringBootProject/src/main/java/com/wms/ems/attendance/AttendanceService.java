package com.wms.ems.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceEventRepository attendanceEventRepository;

    /**
     * Handles clock-in event validation and persistence.
     */
    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String location, Long shiftId) {
        // Geofence validation logic can be added here
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.EventType.IN);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setShiftId(shiftId);
        return attendanceEventRepository.save(event);
    }

    /**
     * Handles clock-out event validation and persistence.
     */
    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String deviceId, String location, Long shiftId) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.EventType.OUT);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setShiftId(shiftId);
        return attendanceEventRepository.save(event);
    }

    /**
     * Calculates total hours worked for a given employee and shift.
     */
    public double calculateHours(Long employeeId, Long shiftId) {
        List<AttendanceEvent> events = attendanceEventRepository.findByEmployeeIdAndShiftIdOrderByTimestampAsc(employeeId, shiftId);
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

    // Additional methods for missed punch handling, corrections, and reporting can be added here.
}
