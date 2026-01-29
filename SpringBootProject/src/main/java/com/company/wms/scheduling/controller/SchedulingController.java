package com.company.wms.scheduling.controller;

import com.company.wms.scheduling.model.Schedule;
import com.company.wms.scheduling.model.ShiftTemplate;
import com.company.wms.scheduling.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for scheduling management.
 */
@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {
    private final SchedulingService schedulingService;

    // Shift Templates
    @GetMapping("/shifts")
    public List<ShiftTemplate> getAllShiftTemplates() {
        return schedulingService.getAllShiftTemplates();
    }

    @GetMapping("/shifts/{id}")
    public ResponseEntity<ShiftTemplate> getShiftTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(schedulingService.getShiftTemplateById(id));
    }

    @PostMapping("/shifts")
    public ResponseEntity<ShiftTemplate> createShiftTemplate(@RequestBody ShiftTemplate template) {
        ShiftTemplate created = schedulingService.createShiftTemplate(template);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Schedules
    @GetMapping("/schedules")
    public List<Schedule> getAllSchedules() {
        return schedulingService.getAllSchedules();
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(schedulingService.getScheduleById(id));
    }

    @GetMapping("/schedules/employee/{employeeId}")
    public List<Schedule> getSchedulesByEmployee(@PathVariable Long employeeId) {
        return schedulingService.getSchedulesByEmployee(employeeId);
    }

    @GetMapping("/schedules/date/{date}")
    public List<Schedule> getSchedulesByDate(@PathVariable LocalDate date) {
        return schedulingService.getSchedulesByDate(date);
    }

    @PostMapping("/schedules")
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule) {
        Schedule created = schedulingService.createSchedule(schedule);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updated = schedulingService.updateSchedule(id, schedule);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        schedulingService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
