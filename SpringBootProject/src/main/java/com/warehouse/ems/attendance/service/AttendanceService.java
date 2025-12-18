package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.dto.ClockInRequestDTO;
import com.warehouse.ems.attendance.dto.ClockOutRequestDTO;
import com.warehouse.ems.attendance.dto.AttendanceResponseDTO;

import java.util.List;

/**
 * Service interface for attendance business logic.
 */
public interface AttendanceService {
    AttendanceResponseDTO clockIn(ClockInRequestDTO request);
    AttendanceResponseDTO clockOut(ClockOutRequestDTO request);
    List<AttendanceResponseDTO> getAttendanceForEmployee(Long employeeId);
    double calculateWorkedHours(Long employeeId, String date);
}
