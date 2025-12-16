package com.company.warehouse.attendance.controller;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.service.AttendanceService;
import com.company.warehouse.attendance.dto.ClockEventDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for attendance clock-in/out endpoints.
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    /**
     * Clock-in endpoint (WORKER, SUPERVISOR, HR, ADMIN).
     */
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody ClockEventDto dto) {
        AttendanceEvent event = attendanceService.clockIn(dto);
        return ResponseEntity.ok(event);
    }

    /**
     * Clock-out endpoint (WORKER, SUPERVISOR, HR, ADMIN).
     */
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody ClockEventDto dto) {
        AttendanceEvent event = attendanceService.clockOut(dto);
        return ResponseEntity.ok(event);
    }

    /**
     * Get attendance events for an employee (ADMIN, HR, SUPERVISOR).
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceEvents(@PathVariable Long employeeId) {
        List<AttendanceEvent> events = attendanceService.getAttendanceEvents(employeeId);
        return ResponseEntity.ok(events);
    }
}
