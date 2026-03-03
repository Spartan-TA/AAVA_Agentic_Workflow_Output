package com.wms.ems.scheduling.service;

import com.wms.ems.scheduling.repository.ShiftTemplateRepository;
import com.wms.ems.scheduling.repository.ShiftAssignmentRepository;
import com.wms.ems.scheduling.dto.ShiftTemplateDto;
import com.wms.ems.scheduling.entity.ShiftTemplate;
import com.wms.ems.scheduling.entity.ShiftAssignment;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service class for Shift scheduling business logic and operations.
 */
@Slf4j
@Service
@Transactional
public class ShiftService {

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Creates a new ShiftTemplate.
     * @param dto ShiftTemplateDto
     * @return ShiftTemplate
     */
    public ShiftTemplate createShiftTemplate(ShiftTemplateDto dto) {
        ShiftTemplate template = new ShiftTemplate(dto);
        return shiftTemplateRepository.save(template);
    }

    /**
     * Assigns a shift to an employee after conflict validation.
     * @param employeeId Employee ID
     * @param templateId ShiftTemplate ID
     * @param date LocalDate
     * @return ShiftAssignment
     */
    public ShiftAssignment assignShift(Long employeeId, Long templateId, LocalDate date) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift template not found: " + templateId));
        if (hasConflict(employeeId, date, template.getStartTime(), template.getEndTime())) {
            throw new ValidationException("Employee has a conflicting shift assignment");
        }
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShiftTemplate(template);
        assignment.setDate(date);
        return shiftAssignmentRepository.save(assignment);
    }

    /**
     * Gets all shift assignments for an employee within a date range.
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List<ShiftAssignment>
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignment> getEmployeeShifts(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    /**
     * Checks if an employee has a conflicting shift assignment.
     * @param employeeId Employee ID
     * @param date LocalDate
     * @param startTime LocalTime
     * @param endTime LocalTime
     * @return boolean
     */
    @Transactional(readOnly = true)
    public boolean hasConflict(Long employeeId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeIdAndDate(employeeId, date);
        for (ShiftAssignment assignment : assignments) {
            LocalTime assignedStart = assignment.getShiftTemplate().getStartTime();
            LocalTime assignedEnd = assignment.getShiftTemplate().getEndTime();
            if (startTime.isBefore(assignedEnd) && endTime.isAfter(assignedStart)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a shift assignment by ID.
     * @param assignmentId Assignment ID
     */
    public void deleteShiftAssignment(Long assignmentId) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found: " + assignmentId));
        shiftAssignmentRepository.delete(assignment);
    }
}
