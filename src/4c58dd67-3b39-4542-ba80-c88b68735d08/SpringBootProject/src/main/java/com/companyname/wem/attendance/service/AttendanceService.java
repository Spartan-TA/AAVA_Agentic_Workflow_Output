package com.companyname.wem.attendance.service;

import com.companyname.wem.attendance.domain.AttendanceEvent;
import com.companyname.wem.attendance.domain.EventType;
import com.companyname.wem.attendance.dto.ClockEventDTO;
import com.companyname.wem.attendance.repository.AttendanceEventRepository;
import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Optional<AttendanceEvent> clockIn(@Valid ClockEventDTO dto) {
        return employeeRepository.findById(dto.getEmployeeId())
                .map(employee -> {
                    AttendanceEvent event = AttendanceEvent.builder()
                            .employee(employee)
                            .type(EventType.CLOCK_IN)
                            .timestamp(LocalDateTime.now())
                            .deviceId(dto.getDeviceId())
                            .latitude(dto.getLatitude())
                            .longitude(dto.getLongitude())
                            .correction(dto.getCorrection() != null ? dto.getCorrection() : false)
                            .build();
                    return attendanceEventRepository.save(event);
                });
    }

    @Transactional
    public Optional<AttendanceEvent> clockOut(@Valid ClockEventDTO dto) {
        return employeeRepository.findById(dto.getEmployeeId())
                .map(employee -> {
                    AttendanceEvent event = AttendanceEvent.builder()
                            .employee(employee)
                            .type(EventType.CLOCK_OUT)
                            .timestamp(LocalDateTime.now())
                            .deviceId(dto.getDeviceId())
                            .latitude(dto.getLatitude())
                            .longitude(dto.getLongitude())
                            .correction(dto.getCorrection() != null ? dto.getCorrection() : false)
                            .build();
                    return attendanceEventRepository.save(event);
                });
    }

    public List<AttendanceEvent> getAttendanceReport(Long employeeId, LocalDateTime start, LocalDateTime end) {
        return employeeRepository.findById(employeeId)
                .map(employee -> attendanceEventRepository.findByEmployeeAndTimestampBetween(employee, start, end))
                .orElse(List.of());
    }

    @Transactional
    public Optional<AttendanceEvent> submitCorrection(@Valid ClockEventDTO dto) {
        return employeeRepository.findById(dto.getEmployeeId())
                .map(employee -> {
                    AttendanceEvent event = AttendanceEvent.builder()
                            .employee(employee)
                            .type(dto.getType())
                            .timestamp(LocalDateTime.now())
                            .deviceId(dto.getDeviceId())
                            .latitude(dto.getLatitude())
                            .longitude(dto.getLongitude())
                            .correction(true)
                            .build();
                    return attendanceEventRepository.save(event);
                });
    }
}
