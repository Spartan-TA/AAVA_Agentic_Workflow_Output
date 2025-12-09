package com.warehouseems.attendance.controller;

import com.warehouseems.attendance.dto.AttendanceDto;
import com.warehouseems.attendance.model.AttendanceEvent;
import com.warehouseems.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for attendance clock-in and clock-out endpoints.
 */
@RestController
@RequestMapping("/attendance")
@Validated
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    /**
     * Endpoint for clock-in.
     */
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody AttendanceDto dto) {
        AttendanceEvent event = attendanceService.clockIn(dto.getEmployeeId(), dto.getDeviceId(), dto.getLocation());
        return ResponseEntity.ok(event);
    }

    /**
     * Endpoint for clock-out.
     */
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody AttendanceDto dto) {
        AttendanceEvent event = attendanceService.clockOut(dto.getEmployeeId(), dto.getDeviceId(), dto.getLocation());
        return ResponseEntity.ok(event);
    }

    // Additional endpoints for corrections and reporting can be added here.
}