package com.warehouse.management.service.impl;

import com.warehouse.management.entity.Shift;
import com.warehouse.management.repository.ShiftRepository;
import com.warehouse.management.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;

    @Autowired
    public ShiftServiceImpl(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Override
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Override
    public Shift updateShift(Long id, Shift shift) {
        shift.setId(id);
        return shiftRepository.save(shift);
    }

    @Override
    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    @Override
    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    @Override
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }
}
