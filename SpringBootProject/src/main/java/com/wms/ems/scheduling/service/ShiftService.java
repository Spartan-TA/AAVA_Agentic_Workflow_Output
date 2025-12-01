package com.wms.ems.scheduling.service;

import com.wms.ems.scheduling.entity.ShiftTemplate;
import com.wms.ems.scheduling.entity.ShiftAssignment;
import com.wms.ems.scheduling.repository.ShiftTemplateRepository;
import com.wms.ems.scheduling.repository.ShiftAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for Shift management.
 * Handles CRUD for templates/assignments and conflict detection.
 */
@Service
@Transactional
public class ShiftService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    public ShiftService(ShiftTemplateRepository shiftTemplateRepository, ShiftAssignmentRepository shiftAssignmentRepository) {
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    // Shift Template CRUD
    public List<ShiftTemplate> getAllTemplates() {
        return shiftTemplateRepository.findAll();
    }

    public ShiftTemplate createTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    public ShiftTemplate updateTemplate(Long id, ShiftTemplate updated) {
        ShiftTemplate existing = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ShiftTemplate not found"));
        existing.setName(updated.getName());
        existing.setActive(updated.isActive());
        // ... update other fields
        return shiftTemplateRepository.save(existing);
    }

    public void deleteTemplate(Long id) {
        shiftTemplateRepository.deleteById(id);
    }

    // Shift Assignment CRUD
    public List<ShiftAssignment> getAssignmentsForEmployee(Long employeeId) {
        return shiftAssignmentRepository.findByEmployeeId(employeeId);
    }

    public ShiftAssignment assignShift(ShiftAssignment assignment) {
        if (hasConflict(assignment.getEmployeeId(), assignment.getShiftDate())) {
            throw new IllegalArgumentException("Shift conflict detected for employee on this date");
        }
        return shiftAssignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        shiftAssignmentRepository.deleteById(id);
    }

    /**
     * Detects if an employee already has a shift on a given date.
     * @param employeeId the employee's ID
     * @param date the date
     * @return true if conflict exists
     */
    public boolean hasConflict(Long employeeId, LocalDate date) {
        return !shiftAssignmentRepository.findByEmployeeId(employeeId).stream()
                .filter(a -> a.getShiftDate().equals(date)).toList().isEmpty();
    }
}
