package com.wms.attendance.controller;

import com.wms.attendance.dto.AttendanceEventDto;
import com.wms.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for Attendance management endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Attendance Management API")
@Validated
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Operation(summary = "Record an attendance event")
    @PostMapping
    public ResponseEntity<AttendanceEventDto> recordAttendance(@Valid @RequestBody AttendanceEventDto attendanceEventDto) {
        return ResponseEntity.ok(attendanceService.recordAttendance(attendanceEventDto));
    }

    @Operation(summary = "Get attendance events by employee ID")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceEventDto>> getAttendanceByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployeeId(employeeId));
    }

    @Operation(summary = "Get all attendance events")
    @GetMapping
    public ResponseEntity<List<AttendanceEventDto>> getAllAttendanceEvents() {
        return ResponseEntity.ok(attendanceService.getAllAttendanceEvents());
    }
}
