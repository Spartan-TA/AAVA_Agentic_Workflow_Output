package com.warehouse.ems.scheduling;

import com.warehouse.ems.common.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing shift assignments and blackout dates.
 */
@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final BlackoutDateRepository blackoutDateRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, BlackoutDateRepository blackoutDateRepository) {
        this.shiftRepository = shiftRepository;
        this.blackoutDateRepository = blackoutDateRepository;
    }

    public List<ShiftAssignment> getAllAssignments() {
        return shiftRepository.findAll();
    }

    public ShiftAssignment getAssignmentById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftAssignment not found with id: " + id));
    }

    public List<ShiftAssignment> getAssignmentsByEmployee(Long employeeId) {
        return shiftRepository.findByEmployeeId(employeeId);
    }

    public List<ShiftAssignment> getAssignmentsByDate(LocalDate date) {
        return shiftRepository.findByAssignmentDate(date);
    }

    @Transactional
    public ShiftAssignment createAssignment(ShiftAssignment assignment) {
        // Check for blackout date
        if (blackoutDateRepository.existsByDate(assignment.getAssignmentDate())) {
            throw new IllegalArgumentException("Cannot assign shift on blackout date: " + assignment.getAssignmentDate());
        }
        return shiftRepository.save(assignment);
    }

    @Transactional
    public ShiftAssignment updateAssignment(Long id, ShiftAssignment updated) {
        ShiftAssignment existing = getAssignmentById(id);
        existing.setEmployeeId(updated.getEmployeeId());
        existing.setShiftId(updated.getShiftId());
        existing.setAssignmentDate(updated.getAssignmentDate());
        existing.setNotes(updated.getNotes());
        return shiftRepository.save(existing);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("ShiftAssignment not found with id: " + id);
        }
        shiftRepository.deleteById(id);
    }
}
