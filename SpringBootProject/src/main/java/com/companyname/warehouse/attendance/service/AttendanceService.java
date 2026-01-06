package com.companyname.warehouse.attendance.service;

import com.companyname.warehouse.attendance.dto.AttendanceRequestDTO;
import com.companyname.warehouse.attendance.dto.AttendanceResponseDTO;
import com.companyname.warehouse.attendance.entity.Attendance;
import com.companyname.warehouse.attendance.mapper.AttendanceMapper;
import com.companyname.warehouse.attendance.repository.AttendanceRepository;
import com.companyname.warehouse.employee.entity.Employee;
import com.companyname.warehouse.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;

/**
 * Service for Attendance business logic.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceResponseDTO clockIn(AttendanceRequestDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        Attendance attendance = attendanceMapper.toEntity(dto);
        attendance.setEmployee(employee);
        attendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(attendance);
    }

    @Transactional
    public AttendanceResponseDTO clockOut(Long attendanceId, AttendanceRequestDTO dto) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found"));
        attendance.setClockOut(dto.getClockOut());
        attendance.setNotes(dto.getNotes());
        attendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(attendance);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponseDTO> getEmployeeAttendance(Long employeeId, Pageable pageable) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return attendanceRepository.findByEmployee(employee, pageable)
                .map(attendanceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public AttendanceResponseDTO getAttendance(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found"));
        return attendanceMapper.toDto(attendance);
    }
}
