package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.repository.AttendanceEventRepository;
import com.company.warehouse.common.exception.ResourceNotFoundException;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for attendance clock-in/out logic.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String deviceInfo, String geoLocation) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .eventTime(LocalDateTime.now())
                .eventType("CLOCK_IN")
                .deviceInfo(deviceInfo)
                .geoLocation(geoLocation)
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String deviceInfo, String geoLocation) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .eventTime(LocalDateTime.now())
                .eventType("CLOCK_OUT")
                .deviceInfo(deviceInfo)
                .geoLocation(geoLocation)
                .build();
        return attendanceEventRepository.save(event);
    }
}