package com.wems.attendance.controller;

import com.wems.attendance.domain.AttendanceEvent;
import com.wems.attendance.dto.ClockEventDto;
import com.wems.attendance.service.AttendanceService;
import com.wems.employee.domain.Employee;
import com.wems.scheduling.domain.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public AttendanceEvent clockIn(@RequestBody ClockEventDto dto) {
        // Employee and Schedule should be resolved from context or service
        Employee employee = null; // TODO: resolve employee
        Schedule schedule = null; // TODO: resolve schedule
        return attendanceService.clockIn(employee, dto, schedule);
    }

    @PostMapping("/clock-out")
    public AttendanceEvent clockOut(@RequestBody ClockEventDto dto) {
        Employee employee = null; // TODO: resolve employee
        Schedule schedule = null; // TODO: resolve schedule
        return attendanceService.clockOut(employee, dto, schedule);
    }

    @PostMapping("/corrections/{eventId}")
    public AttendanceEvent requestCorrection(@PathVariable Long eventId, @RequestParam String reason) {
        return attendanceService.requestCorrection(eventId, reason);
    }

    @PostMapping("/corrections/{eventId}/approve")
    public AttendanceEvent approveCorrection(@PathVariable Long eventId) {
        Employee approver = null; // TODO: resolve approver
        return attendanceService.approveCorrection(eventId, approver);
    }
}
