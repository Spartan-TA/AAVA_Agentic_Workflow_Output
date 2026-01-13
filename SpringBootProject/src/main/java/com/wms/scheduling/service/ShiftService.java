package com.wms.scheduling.service;

import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.scheduling.entity.ShiftAssignment;
import com.wms.scheduling.entity.ShiftTemplate;
import com.wms.scheduling.entity.BlackoutDate;
import com.wms.scheduling.repository.ShiftAssignmentRepository;
import com.wms.scheduling.repository.ShiftTemplateRepository;
import com.wms.scheduling.repository.BlackoutDateRepository;
import com.wms.scheduling.dto.BulkAssignRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for shift and schedule management.
 */
@Service
@RequiredArgsConstructor
public class ShiftService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final BlackoutDateRepository blackoutDateRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    @Transactional
    public ShiftTemplate createShiftTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    @Transactional
    public ShiftAssignment assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) {
        Optional<BlackoutDate> blackout = blackoutDateRepository.findByDate(date);
        if (blackout.isPresent()) {
            throw new RuntimeException("Cannot assign shift on blackout date");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        ShiftTemplate template = shiftTemplateRepository.findById(shiftTemplateId)
                .orElseThrow(() -> new RuntimeException("Shift template not found"));
        // Conflict detection logic would go here
        ShiftAssignment assignment = ShiftAssignment.builder()
                .employee(employee)
                .shiftTemplate(template)
                .date(date)
                .build();
        return shiftAssignmentRepository.save(assignment);
    }

    @Transactional
    public void bulkAssignShifts(BulkAssignRequest request) {
        for (Long employeeId : request.getEmployeeIds()) {
            assignShift(employeeId, request.getShiftTemplateId(), request.getDate());
        }
    }
}
