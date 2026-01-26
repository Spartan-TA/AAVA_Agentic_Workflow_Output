package com.example.warehouse.scheduling.service;

import com.example.warehouse.scheduling.entity.Shift;
import com.example.warehouse.scheduling.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {
    @Autowired
    private ShiftRepository shiftRepository;

    // Get all shifts
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // Get shift by ID
    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    // Create new shift
    @Transactional
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    // Update shift
    @Transactional
    public Optional<Shift> updateShift(Long id, Shift shift) {
        return shiftRepository.findById(id).map(existing -> {
            existing.setName(shift.getName());
            existing.setStartTime(shift.getStartTime());
            existing.setEndTime(shift.getEndTime());
            return shiftRepository.save(existing);
        });
    }

    // Delete shift
    @Transactional
    public boolean deleteShift(Long id) {
        if (shiftRepository.existsById(id)) {
            shiftRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
