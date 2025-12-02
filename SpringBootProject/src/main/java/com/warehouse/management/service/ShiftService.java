package com.warehouse.management.service;

import com.warehouse.management.entity.Shift;
import java.util.List;
import java.util.Optional;

public interface ShiftService {
    Shift createShift(Shift shift);
    Shift updateShift(Long id, Shift shift);
    void deleteShift(Long id);
    Optional<Shift> getShiftById(Long id);
    List<Shift> getAllShifts();
}
