package com.wms.ems.attendance.service.impl;

import com.wms.ems.attendance.model.AttendanceEvent;
import com.wms.ems.attendance.repository.AttendanceRepository;
import com.wms.ems.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public AttendanceEvent clockIn(Long employeeId, Long shiftId, String deviceId, String location, Double latitude, Double longitude) {
        if (!validateGeofence(latitude, longitude, shiftId)) {
            throw new IllegalArgumentException("Geofence validation failed.");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setShiftId(shiftId);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.AttendanceType.CLOCK_IN);
        event.setStatus(AttendanceEvent.AttendanceStatus.NORMAL);
        return attendanceRepository.save(event);
    }

    @Override
    public AttendanceEvent clockOut(Long employeeId, Long shiftId, String deviceId, String location, Double latitude, Double longitude) {
        if (!validateGeofence(latitude, longitude, shiftId)) {
            throw new IllegalArgumentException("Geofence validation failed.");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setShiftId(shiftId);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.AttendanceType.CLOCK_OUT);
        event.setStatus(AttendanceEvent.AttendanceStatus.NORMAL);
        return attendanceRepository.save(event);
    }

    @Override
    public AttendanceEvent requestCorrection(Long attendanceId, String reason) {
        AttendanceEvent event = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance event not found."));
        event.setStatus(AttendanceEvent.AttendanceStatus.CORRECTION_PENDING);
        // Optionally log or store correction reason
        return attendanceRepository.save(event);
    }

    @Override
    public List<AttendanceEvent> getAttendanceForEmployee(Long employeeId, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByEmployeeIdAndTimestampBetween(employeeId, start, end);
    }

    @Override
    public double calculateHoursWorked(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<AttendanceEvent> events = getAttendanceForEmployee(employeeId, start, end);
        double hours = 0.0;
        LocalDateTime lastClockIn = null;
        for (AttendanceEvent event : events) {
            if (event.getType() == AttendanceEvent.AttendanceType.CLOCK_IN) {
                lastClockIn = event.getTimestamp();
            } else if (event.getType() == AttendanceEvent.AttendanceType.CLOCK_OUT && lastClockIn != null) {
                hours += java.time.Duration.between(lastClockIn, event.getTimestamp()).toMinutes() / 60.0;
                lastClockIn = null;
            }
        }
        return hours;
    }

    @Override
    public boolean validateGeofence(Double latitude, Double longitude, Long shiftId) {
        // Placeholder: In real implementation, fetch geofence for shift and validate
        // For demo, always return true
        return true;
    }
}