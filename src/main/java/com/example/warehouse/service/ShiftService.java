package com.example.warehouse.service;

import com.example.warehouse.dto.ShiftDTO;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Shift;
import com.example.warehouse.entity.ShiftTemplate;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.ShiftRepository;
import com.example.warehouse.repository.ShiftTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing shifts.
 */
@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, EmployeeRepository employeeRepository, ShiftTemplateRepository shiftTemplateRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
    }

    /**
     * Get all shifts for an employee.
     * @param employeeId Employee ID
     * @return List of ShiftDTO
     */
    @Transactional(readOnly = true)
    public List<ShiftDTO> getShiftsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return shiftRepository.findByEmployee(employee).stream()
                .map(ShiftDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Assign a shift to an employee.
     * @param employeeId Employee ID
     * @param dto ShiftDTO
     * @return ShiftDTO
     */
    @Transactional
    public ShiftDTO assignShift(Long employeeId, ShiftDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new ValidationException("Shift start and end times are required");
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new ValidationException("Shift end time cannot be before start time");
        }
        Shift shift = new Shift();
        shift.setEmployee(employee);
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        if (dto.getTemplateId() != null) {
            ShiftTemplate template = shiftTemplateRepository.findById(dto.getTemplateId())
                    .orElseThrow(() -> new ValidationException("Invalid shift template ID"));
            shift.setTemplate(template);
        }
        shiftRepository.save(shift);
        return ShiftDTO.fromEntity(shift);
    }

    /**
     * Get all shifts.
     * @return List of ShiftDTO
     */
    @Transactional(readOnly = true)
    public List<ShiftDTO> getAllShifts() {
        return shiftRepository.findAll().stream()
                .map(ShiftDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
