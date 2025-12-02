package com.wms.ems.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShiftService {

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Autowired
    private WarehouseCalendarRepository warehouseCalendarRepository;

    // CRUD for shift templates
    public ShiftTemplate createShiftTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    public ShiftTemplate updateShiftTemplate(Long id, ShiftTemplate updated) {
        ShiftTemplate template = shiftTemplateRepository.findById(id).orElseThrow();
        template.setName(updated.getName());
        template.setStartTime(updated.getStartTime());
        template.setEndTime(updated.getEndTime());
        template.setRecurring(updated.isRecurring());
        return shiftTemplateRepository.save(template);
    }

    public void deleteShiftTemplate(Long id) {
        shiftTemplateRepository.deleteById(id);
    }

    // Conflict detection algorithm
    public boolean hasConflict(Long employeeId, LocalDate date, Long shiftTemplateId) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeIdAndDate(employeeId, date);
        return assignments.stream().anyMatch(a -> a.getShiftTemplateId().equals(shiftTemplateId));
    }

    // Bulk assignment
    @Transactional
    public void bulkAssign(List<ShiftAssignment> assignments) {
        for (ShiftAssignment assignment : assignments) {
            if (!hasConflict(assignment.getEmployeeId(), assignment.getDate(), assignment.getShiftTemplateId())) {
                shiftAssignmentRepository.save(assignment);
            }
        }
    }

    // Blackout date handling
    public boolean isBlackoutDate(LocalDate date) {
        return warehouseCalendarRepository.existsByDateAndType(date, "BLACKOUT");
    }
}
