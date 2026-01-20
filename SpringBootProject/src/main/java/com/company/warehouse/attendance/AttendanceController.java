package com.company.warehouse.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * REST controller for attendance endpoints.
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Operation(summary = "Clock in")
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<ClockEvent> clockIn(@Valid @RequestBody ClockEventDto dto) {
        return new ResponseEntity<>(attendanceService.clockIn(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Clock out")
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<ClockEvent> clockOut(@Valid @RequestBody ClockEventDto dto) {
        return new ResponseEntity<>(attendanceService.clockOut(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Calculate hours worked for a date")
    @GetMapping("/hours")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<Double> calculateHours(@RequestParam Long employeeId, @RequestParam String date) {
        double hours = attendanceService.calculateHours(employeeId, LocalDate.parse(date));
        return ResponseEntity.ok(hours);
    }
}
