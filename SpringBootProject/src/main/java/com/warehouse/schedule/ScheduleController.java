package com.warehouse.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Schedule operations.
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public List<Schedule> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }

    @GetMapping("/employee/{employeeId}")
    public List<Schedule> getSchedulesByEmployee(@PathVariable Long employeeId) {
        return scheduleService.getSchedulesByEmployee(employeeId);
    }

    @GetMapping("/date/{date}")
    public List<Schedule> getSchedulesByDate(@PathVariable String date) {
        return scheduleService.getSchedulesByDate(LocalDate.parse(date));
    }

    @PostMapping
    public Schedule saveSchedule(@RequestBody Schedule schedule) {
        return scheduleService.saveSchedule(schedule);
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
    }
}
