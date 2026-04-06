package com.example.warehouse.service;

import com.example.warehouse.dto.ShiftDTO;
import com.example.warehouse.dto.ShiftAssignmentDTO;
import com.example.warehouse.entity.ShiftTemplate;
import com.example.warehouse.entity.ShiftAssignment;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ShiftConflictException;
import com.example.warehouse.repository.ShiftTemplateRepository;
import com.example.warehouse.repository.ShiftAssignmentRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeRepository employeeRepository;

    public ShiftService(ShiftTemplateRepository shiftTemplateRepository, ShiftAssignmentRepository shiftAssignmentRepository, EmployeeRepository employeeRepository) {
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.employeeRepository = employeeRepository;
    }

    // CRUD for ShiftTemplate
    public ShiftTemplate createShiftTemplate(ShiftDTO dto) {
        ShiftTemplate template = new ShiftTemplate();
        template.setName(dto.getName());
        template.setStartTime(dto.getStartTime());
        template.setEndTime(dto.getEndTime());
        template.setBlackoutDates(dto.getBlackoutDates());
        template.setOvertimeRule(dto.getOvertimeRule());
        return shiftTemplateRepository.save(template);
    }

    public ShiftTemplate updateShiftTemplate(Long id, ShiftDTO dto) {
        ShiftTemplate template = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift template not found"));
        template.setName(dto.getName());
        template.setStartTime(dto.getStartTime());
        template.setEndTime(dto.getEndTime());
        template.setBlackoutDates(dto.getBlackoutDates());
        template.setOvertimeRule(dto.getOvertimeRule());
        return shiftTemplateRepository.save(template);
    }

    public void deleteShiftTemplate(Long id) {
        shiftTemplateRepository.deleteById(id);
    }

    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    // Assignment with conflict detection
    @Transactional
    public ShiftAssignment assignShift(Long employeeId, Long templateId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift template not found"));
        if (template.getBlackoutDates() != null && template.getBlackoutDates().contains(date)) {
            throw new IllegalArgumentException("Cannot assign shift on blackout date");
        }
        boolean conflict = shiftAssignmentRepository.existsByEmployeeIdAndDate(employeeId, date);
        if (conflict) {
            throw new ShiftConflictException("Employee already assigned to a shift on this date");
        }
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShiftTemplate(template);
        assignment.setDate(date);
        return shiftAssignmentRepository.save(assignment);
    }

    // Bulk assignment
    @Transactional
    public void bulkAssignShifts(List<ShiftAssignmentDTO> assignments) {
        for (ShiftAssignmentDTO dto : assignments) {
            assignShift(dto.getEmployeeId(), dto.getShiftTemplateId(), dto.getDate());
        }
    }

    // Overtime rules, blackout dates, etc. can be handled in business logic as needed
}
