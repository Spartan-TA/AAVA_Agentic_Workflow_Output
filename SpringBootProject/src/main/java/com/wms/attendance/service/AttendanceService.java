package com.wms.attendance.service;

import com.wms.attendance.entity.AttendanceEvent;
import com.wms.attendance.repository.AttendanceEventRepository;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.attendance.dto.ClockInRequest;
import com.wms.attendance.dto.ClockOutRequest;
import com.wms.attendance.dto.AttendanceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for attendance logic.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceEvent clockIn(ClockInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        // Geofence validation logic would go here
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .type("CLOCK_IN")
                .timestamp(LocalDateTime.now())
                .location(request.getLocation())
                .deviceId(request.getDeviceId())
                .status("VALID")
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(ClockOutRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .type("CLOCK_OUT")
                .timestamp(LocalDateTime.now())
                .location(request.getLocation())
                .deviceId(request.getDeviceId())
                .status("VALID")
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceEvents(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return attendanceEventRepository.findByEmployee(employee);
    }

    // Missed punch correction workflow would be implemented here
}
