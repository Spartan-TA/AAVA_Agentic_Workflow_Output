package com.example.warehouse.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    public ShiftTemplate createShiftTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    public void deleteShiftTemplate(Long id) {
        shiftTemplateRepository.deleteById(id);
    }

    @Transactional
    public void bulkAssignShifts(BulkAssignDto dto) {
        for (Long employeeId : dto.getEmployeeIds()) {
            Shift shift = new Shift();
            shift.setEmployeeId(employeeId);
            shift.setDate(dto.getDate());
            shift.setStartTime(dto.getStartTime());
            shift.setEndTime(dto.getEndTime());
            shift.setTemplateId(dto.getTemplateId());
            shiftRepository.save(shift);
        }
    }
}
