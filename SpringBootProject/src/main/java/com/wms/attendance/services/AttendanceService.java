package com.wms.attendance.services;

import com.wms.attendance.dtos.ClockEventDto;
import com.wms.attendance.model.AttendanceEvent;
import com.wms.attendance.repositories.AttendanceRepository;
import com.wms.common.exceptions.ResourceNotFoundException;
import com.wms.employee.model.Employee;
import com.wms.employee.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Attendance operations.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    @Autowired
    private final AttendanceRepository attendanceRepository;
    @Autowired
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ClockEventDto clockEvent(ClockEventDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setEventTime(dto.getEventTime());
        event.setEventType(dto.getEventType());
        event.setRemarks(dto.getRemarks());
        AttendanceEvent saved = attendanceRepository.save(event);
        return toDto(saved);
    }

    public List<ClockEventDto> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByEventTimeDesc(employeeId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private ClockEventDto toDto(AttendanceEvent event) {
        ClockEventDto dto = new ClockEventDto();
        dto.setEmployeeId(event.getEmployee().getId());
        dto.setEmployeeName(event.getEmployee().getName());
        dto.setEventTime(event.getEventTime());
        dto.setEventType(event.getEventType());
        dto.setRemarks(event.getRemarks());
        return dto;
    }
}
