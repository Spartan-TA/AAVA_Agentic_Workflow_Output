package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.domain.AttendanceEvent;
import com.warehouse.ems.attendance.dto.ClockInRequest;
import com.warehouse.ems.attendance.dto.ClockOutRequest;
import com.warehouse.ems.attendance.dto.AttendanceDto;
import com.warehouse.ems.attendance.repository.AttendanceRepository;
import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Attendance business logic.
 */
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Clock in an employee.
     * @param request ClockInRequest DTO
     * @return AttendanceDto
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @Transactional
    public AttendanceDto clockIn(ClockInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setClockInTime(LocalDateTime.now());
        event.setStatus("PRESENT");
        event.setNotes(request.getNotes());
        AttendanceEvent saved = attendanceRepository.save(event);
        return toDto(saved);
    }

    /**
     * Clock out an employee.
     * @param request ClockOutRequest DTO
     * @return AttendanceDto
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @Transactional
    public AttendanceDto clockOut(ClockOutRequest request) {
        AttendanceEvent event = attendanceRepository.findById(request.getAttendanceEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance event not found"));
        if (event.getClockOutTime() != null) {
            throw new ValidationException("Already clocked out.");
        }
        event.setClockOutTime(LocalDateTime.now());
        AttendanceEvent updated = attendanceRepository.save(event);
        return toDto(updated);
    }

    /**
     * Get attendance events for an employee.
     * @param employeeId Employee ID
     * @return List of AttendanceDto
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return attendanceRepository.findByEmployee(employee)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance events for an employee within a date range.
     * @param employeeId Employee ID
     * @param start Start date/time
     * @param end End date/time
     * @return List of AttendanceDto
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceByEmployeeAndDateRange(Long employeeId, LocalDateTime start, LocalDateTime end) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return attendanceRepository.findByEmployeeAndClockInTimeBetween(employee, start, end)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert AttendanceEvent entity to AttendanceDto.
     * @param event AttendanceEvent entity
     * @return AttendanceDto
     */
    private AttendanceDto toDto(AttendanceEvent event) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(event.getId());
        dto.setEmployeeId(event.getEmployee().getId());
        dto.setClockInTime(event.getClockInTime());
        dto.setClockOutTime(event.getClockOutTime());
        dto.setStatus(event.getStatus());
        dto.setNotes(event.getNotes());
        return dto;
    }
}
