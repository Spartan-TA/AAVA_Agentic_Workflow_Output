package com.warehouse.employee.controller;

import com.warehouse.employee.dto.AttendanceResponse;
import com.warehouse.employee.dto.ClockInRequest;
import com.warehouse.employee.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

/**
 * REST controller for attendance management.
 */
@RestController
@RequestMapping("/api/attendance")
@Validated
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Operation(summary = "Clock in for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clock-in successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody ClockInRequest request) {
        AttendanceResponse response = attendanceService.clockIn(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Clock out for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clock-out successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(@Valid @RequestBody ClockInRequest request) {
        AttendanceResponse response = attendanceService.clockOut(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get daily hours worked for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hours calculated")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','MANAGER')")
    @GetMapping("/daily-hours/{employeeId}")
    public ResponseEntity<Double> getDailyHours(@PathVariable Long employeeId,
                                                @RequestParam String date) {
        double hours = attendanceService.calculateDailyHours(employeeId, LocalDate.parse(date));
        return ResponseEntity.ok(hours);
    }
}
