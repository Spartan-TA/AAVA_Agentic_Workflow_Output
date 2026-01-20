package com.wms.attendance.service;

import com.wms.attendance.dto.AttendanceEventDto;
import java.util.List;

/**
 * Service interface for Attendance operations.
 * Defines business logic methods for attendance management.
 */
public interface AttendanceService {
    AttendanceEventDto recordAttendance(AttendanceEventDto attendanceEventDto);
    List<AttendanceEventDto> getAttendanceByEmployeeId(Long employeeId);
    List<AttendanceEventDto> getAllAttendanceEvents();
}
