package com.company.wms.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing shifts and assignments.
 */
@Service
public class ShiftService {
    private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, ShiftAssignmentRepository shiftAssignmentRepository) {
        this.shiftRepository = shiftRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    public List<ShiftTemplate> getAllShifts() {
        logger.info("Fetching all shift templates");
        return shiftRepository.findAll();
    }

    public Optional<ShiftTemplate> getShiftById(Long id) {
        logger.info("Fetching shift template with id {}", id);
        return shiftRepository.findById(id);
    }

    @Transactional
    public ShiftTemplate createShift(@Valid @NotNull ShiftTemplate shiftTemplate) {
        logger.info("Creating new shift template: {}", shiftTemplate.getName());
        return shiftRepository.save(shiftTemplate);
    }

    @Transactional
    public ShiftTemplate updateShift(Long id, @Valid @NotNull ShiftTemplate shiftTemplate) {
        logger.info("Updating shift template with id {}", id);
        shiftTemplate.setId(id);
        return shiftRepository.save(shiftTemplate);
    }

    @Transactional
    public void deleteShift(Long id) {
        logger.info("Deleting shift template with id {}", id);
        shiftRepository.deleteById(id);
    }

    public List<ShiftAssignment> getAssignmentsByEmployee(Long employeeId) {
        logger.info("Fetching assignments for employee {}", employeeId);
        return shiftAssignmentRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public ShiftAssignment assignShift(@Valid @NotNull ShiftAssignment assignment) {
        logger.info("Assigning shift {} to employee {} on {}", assignment.getShiftTemplate().getId(), assignment.getEmployeeId(), assignment.getAssignmentDate());
        return shiftAssignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        logger.info("Deleting shift assignment with id {}", assignmentId);
        shiftAssignmentRepository.deleteById(assignmentId);
    }
}
