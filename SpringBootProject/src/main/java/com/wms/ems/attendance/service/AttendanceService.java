package com.wms.ems.attendance.service;

import com.wms.ems.attendance.model.AttendanceEvent;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {
    AttendanceEvent clockIn(Long employeeId, Long shiftId, String deviceId, String location, Double latitude, Double longitude);
    AttendanceEvent clockOut(Long employeeId, Long shiftId, String deviceId, String location, Double latitude, Double longitude);
    AttendanceEvent requestCorrection(Long attendanceId, String reason);
    List<AttendanceEvent> getAttendanceForEmployee(Long employeeId, LocalDateTime start, LocalDateTime end);
    double calculateHoursWorked(Long employeeId, LocalDateTime start, LocalDateTime end);
    boolean validateGeofence(Double latitude, Double longitude, Long shiftId);
}