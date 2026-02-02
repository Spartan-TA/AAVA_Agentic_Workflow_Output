package com.wms.scheduling.services;

import com.wms.scheduling.dtos.ShiftAssignmentDto;
import com.wms.scheduling.dtos.ShiftTemplateDto;
import com.wms.scheduling.model.ShiftAssignment;
import com.wms.scheduling.model.ShiftTemplate;
import com.wms.scheduling.repositories.ShiftAssignmentRepository;
import com.wms.scheduling.repositories.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing shift templates and assignments
 */
@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * Create a new shift template
     */
    public ShiftTemplateDto createShiftTemplate(ShiftTemplateDto dto) {
        ShiftTemplate template = ShiftTemplate.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .active(dto.isActive())
                .build();
        ShiftTemplate saved = shiftTemplateRepository.save(template);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all shift templates
     */
    public List<ShiftTemplateDto> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll().stream()
                .map(t -> ShiftTemplateDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .active(t.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Assign a shift to an employee
     */
    @Transactional
    public ShiftAssignmentDto assignShift(ShiftAssignmentDto dto) {
        Optional<ShiftTemplate> templateOpt = shiftTemplateRepository.findById(dto.getShiftTemplateId());
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("Shift template not found");
        }
        ShiftAssignment assignment = ShiftAssignment.builder()
                .employeeId(dto.getEmployeeId())
                .shiftTemplate(templateOpt.get())
                .shiftDate(dto.getShiftDate())
                .active(dto.isActive())
                .build();
        ShiftAssignment saved = shiftAssignmentRepository.save(assignment);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all assignments for an employee
     */
    public List<ShiftAssignmentDto> getAssignmentsForEmployee(Long employeeId) {
        return shiftAssignmentRepository.findByEmployeeId(employeeId).stream()
                .map(a -> ShiftAssignmentDto.builder()
                        .id(a.getId())
                        .employeeId(a.getEmployeeId())
                        .shiftTemplateId(a.getShiftTemplate().getId())
                        .shiftDate(a.getShiftDate())
                        .active(a.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get all assignments for a date
     */
    public List<ShiftAssignmentDto> getAssignmentsForDate(LocalDate date) {
        return shiftAssignmentRepository.findByShiftDate(date).stream()
                .map(a -> ShiftAssignmentDto.builder()
                        .id(a.getId())
                        .employeeId(a.getEmployeeId())
                        .shiftTemplateId(a.getShiftTemplate().getId())
                        .shiftDate(a.getShiftDate())
                        .active(a.isActive())
                        .build())
                .collect(Collectors.toList());
    }
}
