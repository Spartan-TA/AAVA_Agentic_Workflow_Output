package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.repository.AttendanceRepository;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.attendance.dto.ClockEventDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for attendance clock-in/out and business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Clock-in event for an employee.
     */
    public AttendanceEvent clockIn(ClockEventDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .type("CLOCK_IN")
                .timestamp(LocalDateTime.now())
                .deviceId(dto.getDeviceId())
                .location(dto.getLocation())
                .status("VALID")
                .build();
        return attendanceRepository.save(event);
    }

    /**
     * Clock-out event for an employee.
     */
    public AttendanceEvent clockOut(ClockEventDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .type("CLOCK_OUT")
                .timestamp(LocalDateTime.now())
                .deviceId(dto.getDeviceId())
                .location(dto.getLocation())
                .status("VALID")
                .build();
        return attendanceRepository.save(event);
    }

    /**
     * Get attendance events for an employee.
     */
    public List<AttendanceEvent> getAttendanceEvents(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    // Additional methods for corrections, approvals, and reporting can be added here
}
