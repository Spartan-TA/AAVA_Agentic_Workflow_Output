package com.wms.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for attendance management.
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Time & Attendance APIs")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Operation(summary = "Clock in")
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<AttendanceRecordDTO> clockIn(
            @RequestParam Long employeeId,
            @RequestParam String deviceInfo) {
        return ResponseEntity.ok(attendanceService.clockIn(employeeId, deviceInfo));
    }

    @Operation(summary = "Clock out")
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<AttendanceRecordDTO> clockOut(
            @RequestParam Long employeeId,
            @RequestParam String deviceInfo) {
        return ResponseEntity.ok(attendanceService.clockOut(employeeId, deviceInfo));
    }

    @Operation(summary = "Get attendance records for employee")
    @GetMapping("/records/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<List<AttendanceRecordDTO>> getRecords(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getRecords(employeeId));
    }
}