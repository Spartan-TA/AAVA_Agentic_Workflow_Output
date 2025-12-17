package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.ShiftAssignment;
import com.warehouse.employee.management.repository.ShiftAssignmentRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing ShiftAssignment entities.
 */
@Service
public class ShiftAssignmentService {
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    public ShiftAssignmentService(ShiftAssignmentRepository shiftAssignmentRepository) {
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    /**
     * Get all shift assignments.
     * @return List of shift assignments
     */
    public List<ShiftAssignment> getAllShiftAssignments() {
        return shiftAssignmentRepository.findAll();
    }

    /**
     * Get shift assignment by ID.
     * @param id ShiftAssignment ID
     * @return ShiftAssignment entity
     */
    public ShiftAssignment getShiftAssignmentById(Long id) {
        return shiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftAssignment not found with id: " + id));
    }

    /**
     * Create a new shift assignment.
     * @param shiftAssignment ShiftAssignment entity
     * @return Created shift assignment
     */
    @Transactional
    public ShiftAssignment createShiftAssignment(ShiftAssignment shiftAssignment) {
        // TODO: Add conflict detection logic
        return shiftAssignmentRepository.save(shiftAssignment);
    }

    /**
     * Update an existing shift assignment.
     * @param id ShiftAssignment ID
     * @param updatedAssignment Updated shift assignment entity
     * @return Updated shift assignment
     */
    @Transactional
    public ShiftAssignment updateShiftAssignment(Long id, ShiftAssignment updatedAssignment) {
        ShiftAssignment existingAssignment = getShiftAssignmentById(id);
        existingAssignment.setEmployee(updatedAssignment.getEmployee());
        existingAssignment.setShift(updatedAssignment.getShift());
        existingAssignment.setAssignmentDate(updatedAssignment.getAssignmentDate());
        // Add other fields as needed
        return shiftAssignmentRepository.save(existingAssignment);
    }

    /**
     * Delete a shift assignment by ID.
     * @param id ShiftAssignment ID
     */
    @Transactional
    public void deleteShiftAssignment(Long id) {
        ShiftAssignment assignment = getShiftAssignmentById(id);
        shiftAssignmentRepository.delete(assignment);
    }
}
