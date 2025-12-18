package com.warehouse.ems.attendance.controller;

import com.warehouse.ems.attendance.dto.ClockInRequestDTO;
import com.warehouse.ems.attendance.dto.ClockOutRequestDTO;
import com.warehouse.ems.attendance.dto.AttendanceResponseDTO;
import com.warehouse.ems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for attendance clock in/out operations.
 */
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Attendance management endpoints")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Operation(summary = "Clock in for an employee")
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponseDTO> clockIn(@Valid @RequestBody ClockInRequestDTO request) {
        return ResponseEntity.ok(attendanceService.clockIn(request));
    }

    @Operation(summary = "Clock out for an employee")
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponseDTO> clockOut(@Valid @RequestBody ClockOutRequestDTO request) {
        return ResponseEntity.ok(attendanceService.clockOut(request));
    }

    @Operation(summary = "Get attendance events for an employee")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getAttendanceForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceForEmployee(employeeId));
    }

    @Operation(summary = "Calculate worked hours for an employee on a date")
    @GetMapping("/employee/{employeeId}/hours")
    public ResponseEntity<Double> calculateWorkedHours(@PathVariable Long employeeId,
                                                      @RequestParam String date) {
        return ResponseEntity.ok(attendanceService.calculateWorkedHours(employeeId, date));
    }
}
