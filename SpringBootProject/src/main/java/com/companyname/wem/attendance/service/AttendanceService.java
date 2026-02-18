package com.companyname.wem.attendance.service;

import com.companyname.wem.attendance.domain.AttendanceEvent;
import com.companyname.wem.attendance.domain.EventType;
import com.companyname.wem.attendance.dto.ClockEventDTO;
import com.companyname.wem.attendance.repository.AttendanceEventRepository;
import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceEventRepository repository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceEvent clockIn(ClockEventDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .type(EventType.CLOCK_IN)
            .timestamp(dto.getTimestamp())
            .deviceId(dto.getDeviceId())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .correction(false)
            .build();
        
        return repository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(ClockEventDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .type(EventType.CLOCK_OUT)
            .timestamp(dto.getTimestamp())
            .deviceId(dto.getDeviceId())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .correction(false)
            .build();
        
        return repository.save(event);
    }
}
