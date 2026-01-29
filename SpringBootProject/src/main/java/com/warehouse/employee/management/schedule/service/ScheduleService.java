package com.warehouse.employee.management.schedule.service;

import com.warehouse.employee.management.schedule.domain.Shift;
import com.warehouse.employee.management.schedule.repository.ShiftRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {
    @Autowired
    private ShiftRepo shiftRepo;

    public List<Shift> getAllShifts() {
        return shiftRepo.findAll();
    }

    public Optional<Shift> getShiftById(Long id) {
        return shiftRepo.findById(id);
    }

    public Shift saveShift(Shift shift) {
        return shiftRepo.save(shift);
    }

    public void deleteShift(Long id) {
        shiftRepo.deleteById(id);
    }
}
