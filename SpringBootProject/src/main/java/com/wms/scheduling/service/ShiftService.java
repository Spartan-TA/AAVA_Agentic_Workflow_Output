package com.wms.scheduling.service;

import com.wms.scheduling.dto.ShiftTemplateDto;
import com.wms.scheduling.dto.ShiftAssignmentDto;
import java.util.List;

/**
 * Service interface for Shift operations.
 * Defines business logic methods for shift scheduling.
 */
public interface ShiftService {
    ShiftTemplateDto createShiftTemplate(ShiftTemplateDto shiftTemplateDto);
    List<ShiftTemplateDto> getAllShiftTemplates();
    ShiftAssignmentDto assignShift(ShiftAssignmentDto shiftAssignmentDto);
    List<ShiftAssignmentDto> getAssignmentsByEmployeeId(Long employeeId);
    List<ShiftAssignmentDto> getAssignmentsByDate(java.time.LocalDate assignmentDate);
}
