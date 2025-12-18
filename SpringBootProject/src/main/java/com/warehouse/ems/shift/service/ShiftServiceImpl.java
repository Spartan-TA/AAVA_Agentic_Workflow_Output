package com.warehouse.ems.shift.service;

import com.warehouse.ems.shift.entity.ShiftAssignment;
import com.warehouse.ems.shift.entity.ShiftTemplate;
import com.warehouse.ems.shift.repository.ShiftAssignmentRepository;
import com.warehouse.ems.shift.repository.ShiftTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of ShiftService with conflict detection logic.
 */
@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Override
    public ShiftTemplate createShiftTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    @Override
    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    @Override
    public ShiftAssignment assignShift(Long employeeId, Long templateId, LocalDate date) {
        if (hasShiftConflict(employeeId, date, templateId)) {
            throw new RuntimeException("Shift conflict detected for employee on this date.");
        }
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Shift template not found"));
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployeeId(employeeId);
        assignment.setShiftTemplate(template);
        assignment.setShiftDate(date);
        return shiftAssignmentRepository.save(assignment);
    }

    @Override
    public List<ShiftAssignment> getAssignmentsForEmployee(Long employeeId) {
        return shiftAssignmentRepository.findByEmployeeId(employeeId);
    }

    @Override
    public boolean hasShiftConflict(Long employeeId, LocalDate date, Long templateId) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeId(employeeId);
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getShiftDate().equals(date)) {
                // Conflict: already assigned a shift on this date
                return true;
            }
        }
        List<ShiftAssignment> templateAssignments = shiftAssignmentRepository.findAssignmentsForTemplateOnDate(templateId, date);
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Shift template not found"));
        if (templateAssignments.size() >= template.getMaxEmployees()) {
            // Conflict: shift template is full for this date
            return true;
        }
        return false;
    }
}
