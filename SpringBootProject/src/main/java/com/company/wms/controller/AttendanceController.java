package com.company.wms.controller;

import com.company.wms.domain.AttendanceEvent;
import com.company.wms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for attendance events (clock in/out).
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','HR','MANAGER')")
    public ResponseEntity<AttendanceEvent> clockIn(@PathVariable Long employeeId) {
        return new ResponseEntity<>(attendanceService.clockIn(employeeId), HttpStatus.CREATED);
    }

    @PostMapping("/clock-out/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','HR','MANAGER')")
    public ResponseEntity<AttendanceEvent> clockOut(@PathVariable Long employeeId) {
        return new ResponseEntity<>(attendanceService.clockOut(employeeId), HttpStatus.CREATED);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','HR','MANAGER')")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceEvents(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceEvents(employeeId));
    }
}
