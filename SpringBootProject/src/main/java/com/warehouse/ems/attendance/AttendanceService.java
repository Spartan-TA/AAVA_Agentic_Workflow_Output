package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for attendance clock-in/out and hours calculation.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String location) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(AttendanceEvent.AttendanceType.CLOCK_IN)
                .deviceId(deviceId)
                .location(location)
                .build();
        return attendanceRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(Long employeeId, String deviceId, String location) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(AttendanceEvent.AttendanceType.CLOCK_OUT)
                .deviceId(deviceId)
                .location(location)
                .build();
        return attendanceRepository.save(event);
    }

    @Transactional(readOnly = true)
    public long calculateWorkedHours(Long employeeId, LocalDateTime from, LocalDateTime to) {
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeIdOrderByTimestampDesc(employeeId);
        long totalSeconds = 0;
        LocalDateTime lastClockIn = null;
        for (AttendanceEvent event : events) {
            if (event.getTimestamp().isBefore(from) || event.getTimestamp().isAfter(to)) continue;
            if (event.getType() == AttendanceEvent.AttendanceType.CLOCK_IN) {
                lastClockIn = event.getTimestamp();
            } else if (event.getType() == AttendanceEvent.AttendanceType.CLOCK_OUT && lastClockIn != null) {
                totalSeconds += Duration.between(lastClockIn, event.getTimestamp()).getSeconds();
                lastClockIn = null;
            }
        }
        return totalSeconds / 3600;
    }
}
