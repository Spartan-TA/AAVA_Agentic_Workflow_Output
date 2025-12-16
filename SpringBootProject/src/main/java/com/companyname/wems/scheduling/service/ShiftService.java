package com.companyname.wems.scheduling.service;

import com.companyname.wems.scheduling.model.ShiftTemplate;
import com.companyname.wems.scheduling.model.ShiftAssignment;
import com.companyname.wems.scheduling.repository.ShiftTemplateRepository;
import com.companyname.wems.scheduling.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    // CRUD for ShiftTemplate
    public ShiftTemplate createShiftTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    public ShiftTemplate updateShiftTemplate(Long id, ShiftTemplate updated) {
        ShiftTemplate existing = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ShiftTemplate not found"));
        existing.setName(updated.getName());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setDaysOfWeek(updated.getDaysOfWeek());
        existing.setBlackoutDates(updated.getBlackoutDates());
        return shiftTemplateRepository.save(existing);
    }

    public void deleteShiftTemplate(Long id) {
        shiftTemplateRepository.deleteById(id);
    }

    // Bulk shift assignment with conflict detection
    public List<ShiftAssignment> assignShiftsBulk(List<ShiftAssignment> assignments) {
        return assignments.stream().map(this::assignShiftWithConflictDetection).collect(Collectors.toList());
    }

    public ShiftAssignment assignShiftWithConflictDetection(ShiftAssignment assignment) {
        // Check for blackout date
        ShiftTemplate template = shiftTemplateRepository.findById(assignment.getShiftTemplateId())
                .orElseThrow(() -> new RuntimeException("ShiftTemplate not found"));
        if (template.getBlackoutDates() != null && template.getBlackoutDates().contains(assignment.getAssignmentDate().atStartOfDay())) {
            assignment.setStatus("CONFLICT");
        } else {
            // Check for overlapping assignments for the employee
            List<ShiftAssignment> existingAssignments = shiftAssignmentRepository.findByEmployeeId(assignment.getEmployeeId());
            boolean conflict = existingAssignments.stream().anyMatch(a -> a.getAssignmentDate().equals(assignment.getAssignmentDate()) && a.getShiftTemplateId().equals(assignment.getShiftTemplateId()));
            assignment.setStatus(conflict ? "CONFLICT" : "ASSIGNED");
        }
        return shiftAssignmentRepository.save(assignment);
    }

    public List<ShiftAssignment> getEmployeeShifts(Long employeeId) {
        return shiftAssignmentRepository.findByEmployeeId(employeeId);
    }
}