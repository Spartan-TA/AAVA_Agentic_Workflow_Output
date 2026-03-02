package com.wems.scheduling.controller;

import com.wems.scheduling.domain.Schedule;
import com.wems.scheduling.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/my")
    public List<Schedule> getMySchedules(@RequestParam Long employeeId) {
        return scheduleService.getEmployeeSchedules(employeeId);
    }

    @PostMapping
    public Schedule createSchedule(@RequestParam Long employeeId, @RequestParam Long shiftTemplateId, @RequestParam String date, @RequestParam(required = false) String notes) {
        return scheduleService.createSchedule(employeeId, shiftTemplateId, LocalDate.parse(date), notes);
    }

    @PostMapping("/bulk-assign")
    public List<Schedule> bulkAssign(@RequestBody BulkAssignDto dto) {
        return scheduleService.bulkAssign(dto.getEmployeeIds(), dto.getShiftTemplateId(), dto.getDate());
    }

    @DeleteMapping("/{id}")
    public void cancelSchedule(@PathVariable Long id) {
        scheduleService.cancelSchedule(id);
    }
}

class BulkAssignDto {
    private List<Long> employeeIds;
    private Long shiftTemplateId;
    private LocalDate date;
    public List<Long> getEmployeeIds() { return employeeIds; }
    public void setEmployeeIds(List<Long> employeeIds) { this.employeeIds = employeeIds; }
    public Long getShiftTemplateId() { return shiftTemplateId; }
    public void setShiftTemplateId(Long shiftTemplateId) { this.shiftTemplateId = shiftTemplateId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
