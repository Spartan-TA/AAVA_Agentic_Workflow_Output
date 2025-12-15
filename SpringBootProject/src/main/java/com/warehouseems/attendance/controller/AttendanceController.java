package com.warehouseems.attendance.controller;

import com.warehouseems.attendance.dto.ClockInDto;
import com.warehouseems.attendance.dto.ClockOutDto;
import com.warehouseems.attendance.entity.AttendanceEvent;
import com.warehouseems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for attendance endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management endpoints")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Operation(summary = "Clock in", description = "Clock in for an employee.")
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public AttendanceEvent clockIn(@Validated @RequestBody ClockInDto dto) {
        return attendanceService.clockIn(dto);
    }

    @Operation(summary = "Clock out", description = "Clock out for an employee.")
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public AttendanceEvent clockOut(@Validated @RequestBody ClockOutDto dto) {
        return attendanceService.clockOut(dto);
    }

    @Operation(summary = "Get employee attendance", description = "Get attendance events for an employee on a given date.")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public List<AttendanceEvent> getEmployeeAttendance(@PathVariable Long employeeId, @RequestParam LocalDate date) {
        return attendanceService.getEmployeeAttendance(employeeId, date);
    }
}
