package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.ShiftDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/schedules")
@Validated
public class ScheduleController {
    private final List<ShiftDto> shifts = new ArrayList<>();

    @PreAuthorize("hasAuthority('SCHEDULE_CREATE')")
    @PostMapping("/shifts")
    public ShiftDto createShift(@Valid @RequestBody ShiftDto shiftDto) {
        if (hasConflict(shiftDto)) throw new IllegalArgumentException("Shift conflict detected");
        shifts.add(shiftDto);
        return shiftDto;
    }

    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    @GetMapping("/shifts")
    public List<ShiftDto> getShifts() {
        return Collections.unmodifiableList(shifts);
    }

    private boolean hasConflict(ShiftDto newShift) {
        for (ShiftDto s : shifts) {
            if (s.getDepartmentId().equals(newShift.getDepartmentId()) &&
                (s.getStartTime().equals(newShift.getStartTime()) ||
                 s.getEndTime().equals(newShift.getEndTime()))) {
                return true;
            }
        }
        return false;
    }
}
