package com.example.warehouse.service;

import com.example.warehouse.dto.AttendanceEventDTO;
import com.example.warehouse.entity.AttendanceEvent;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.AttendanceEventRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing attendance events.
 */
@Service
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceService(AttendanceEventRepository attendanceEventRepository, EmployeeRepository employeeRepository) {
        this.attendanceEventRepository = attendanceEventRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all attendance events for an employee.
     * @param employeeId Employee ID
     * @return List of AttendanceEventDTO
     */
    @Transactional(readOnly = true)
    public List<AttendanceEventDTO> getAttendanceByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return attendanceEventRepository.findByEmployee(employee).stream()
                .map(AttendanceEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Record a new attendance event.
     * @param employeeId Employee ID
     * @param dto AttendanceEventDTO
     * @return AttendanceEventDTO
     */
    @Transactional
    public AttendanceEventDTO recordAttendance(Long employeeId, AttendanceEventDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getEventType() == null) {
            throw new ValidationException("Event type is required");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setEventType(dto.getEventType());
        event.setEventTime(dto.getEventTime() != null ? dto.getEventTime() : LocalDateTime.now());
        attendanceEventRepository.save(event);
        return AttendanceEventDTO.fromEntity(event);
    }

    /**
     * Get all attendance events.
     * @return List of AttendanceEventDTO
     */
    @Transactional(readOnly = true)
    public List<AttendanceEventDTO> getAllAttendanceEvents() {
        return attendanceEventRepository.findAll().stream()
                .map(AttendanceEventDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
