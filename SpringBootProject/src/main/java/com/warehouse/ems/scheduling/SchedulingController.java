package com.warehouse.ems.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @Autowired
    private SchedulingService schedulingService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PostMapping("/shifts")
    public ResponseEntity<Shift> createShift(@RequestBody Shift shift) {
        return new ResponseEntity<>(schedulingService.createShift(shift), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('SUPERVISOR')")
    @GetMapping("/shifts")
    public ResponseEntity<List<Shift>> getAllShifts() {
        return ResponseEntity.ok(schedulingService.getAllShifts());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('SUPERVISOR')")
    @PostMapping("/assign")
    public ResponseEntity<Schedule> assignShift(@RequestParam Long employeeId,
                                                @RequestParam Long shiftId,
                                                @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return new ResponseEntity<>(schedulingService.assignShift(employeeId, shiftId, localDate), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('SUPERVISOR') or hasRole('WORKER')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Schedule>> getSchedulesForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(schedulingService.getSchedulesForEmployee(employeeId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('SUPERVISOR')")
    @GetMapping("/schedule/{id}")
    public ResponseEntity<Schedule> getSchedule(@PathVariable Long id) {
        Optional<Schedule> schedule = schedulingService.getSchedule(id);
        return schedule.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
