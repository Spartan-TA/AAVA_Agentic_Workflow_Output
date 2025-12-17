package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.Shift;
import com.warehouse.employee.management.repository.ShiftRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Shift entities.
 */
@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    /**
     * Get all shifts.
     * @return List of shifts
     */
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    /**
     * Get shift by ID.
     * @param id Shift ID
     * @return Shift entity
     */
    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
    }

    /**
     * Create a new shift.
     * @param shift Shift entity
     * @return Created shift
     */
    @Transactional
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    /**
     * Update an existing shift.
     * @param id Shift ID
     * @param updatedShift Updated shift entity
     * @return Updated shift
     */
    @Transactional
    public Shift updateShift(Long id, Shift updatedShift) {
        Shift existingShift = getShiftById(id);
        existingShift.setName(updatedShift.getName());
        existingShift.setStartTime(updatedShift.getStartTime());
        existingShift.setEndTime(updatedShift.getEndTime());
        existingShift.setDescription(updatedShift.getDescription());
        // Add other fields as needed
        return shiftRepository.save(existingShift);
    }

    /**
     * Delete a shift by ID.
     * @param id Shift ID
     */
    @Transactional
    public void deleteShift(Long id) {
        Shift shift = getShiftById(id);
        shiftRepository.delete(shift);
    }
}
