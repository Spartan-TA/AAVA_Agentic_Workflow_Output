package com.warehouse.ems.shift.service;

import com.warehouse.ems.shift.entity.ShiftAssignment;
import com.warehouse.ems.shift.entity.ShiftTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for shift business logic.
 */
public interface ShiftService {
    ShiftTemplate createShiftTemplate(ShiftTemplate template);
    List<ShiftTemplate> getAllShiftTemplates();
    ShiftAssignment assignShift(Long employeeId, Long templateId, LocalDate date);
    List<ShiftAssignment> getAssignmentsForEmployee(Long employeeId);
    boolean hasShiftConflict(Long employeeId, LocalDate date, Long templateId);
}
