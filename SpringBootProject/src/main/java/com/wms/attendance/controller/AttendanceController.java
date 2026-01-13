package com.wms.attendance.controller;

import com.wms.attendance.dto.ClockInRequest;
import com.wms.attendance.dto.ClockOutRequest;
import com.wms.attendance.entity.AttendanceEvent;
import com.wms.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for attendance endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody ClockInRequest request) {
        return new ResponseEntity<>(attendanceService.clockIn(request), HttpStatus.CREATED);
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody ClockOutRequest request) {
        return new ResponseEntity<>(attendanceService.clockOut(request), HttpStatus.CREATED);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceEvents(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceEvents(employeeId));
    }
}
