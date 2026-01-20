package com.wms.scheduling.service;

import com.wms.scheduling.dto.ShiftTemplateDto;
import com.wms.scheduling.dto.ShiftAssignmentDto;
import com.wms.scheduling.domain.ShiftTemplate;
import com.wms.scheduling.domain.ShiftAssignment;
import com.wms.scheduling.repository.ShiftTemplateRepository;
import com.wms.scheduling.repository.ShiftAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ShiftService interface.
 * Handles business logic for shift scheduling.
 */
@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Override
    public ShiftTemplateDto createShiftTemplate(ShiftTemplateDto shiftTemplateDto) {
        ShiftTemplate template = new ShiftTemplate(
            shiftTemplateDto.getName(),
            shiftTemplateDto.getStartTime(),
            shiftTemplateDto.getEndTime()
        );
        ShiftTemplate saved = shiftTemplateRepository.save(template);
        return new ShiftTemplateDto(saved);
    }

    @Override
    public List<ShiftTemplateDto> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll().stream()
            .map(ShiftTemplateDto::new)
            .collect(Collectors.toList());
    }

    @Override
    public ShiftAssignmentDto assignShift(ShiftAssignmentDto shiftAssignmentDto) {
        ShiftAssignment assignment = new ShiftAssignment(
            shiftAssignmentDto.getEmployeeId(),
            shiftAssignmentDto.getShiftTemplateId(),
            shiftAssignmentDto.getAssignmentDate()
        );
        ShiftAssignment saved = shiftAssignmentRepository.save(assignment);
        return new ShiftAssignmentDto(saved);
    }

    @Override
    public List<ShiftAssignmentDto> getAssignmentsByEmployeeId(Long employeeId) {
        return shiftAssignmentRepository.findByEmployeeId(employeeId).stream()
            .map(ShiftAssignmentDto::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<ShiftAssignmentDto> getAssignmentsByDate(java.time.LocalDate assignmentDate) {
        return shiftAssignmentRepository.findByAssignmentDate(assignmentDate).stream()
            .map(ShiftAssignmentDto::new)
            .collect(Collectors.toList());
    }
}
