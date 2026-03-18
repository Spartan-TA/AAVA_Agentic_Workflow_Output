package com.company.wms.service;

import com.company.wms.domain.AttendanceEvent;
import com.company.wms.domain.Employee;
import com.company.wms.repository.AttendanceEventRepository;
import com.company.wms.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling attendance events (clock in/out).
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceEvent clockIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .eventTime(LocalDateTime.now())
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .eventTime(LocalDateTime.now())
                .eventType(AttendanceEvent.EventType.CLOCK_OUT)
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceEvents(Long employeeId) {
        return attendanceEventRepository.findByEmployeeIdOrderByEventTimeDesc(employeeId);
    }
}
