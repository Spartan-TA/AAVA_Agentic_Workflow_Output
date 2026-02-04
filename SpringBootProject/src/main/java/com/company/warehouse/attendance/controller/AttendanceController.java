package com.company.warehouse.attendance.controller;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for attendance clock-in/out.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Clock in for an employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @PostMapping("/clock-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceEvent clockIn(@RequestParam @NotNull Long employeeId,
                                   @RequestParam(required = false) String deviceInfo,
                                   @RequestParam(required = false) String geoLocation) {
        return attendanceService.clockIn(employeeId, deviceInfo, geoLocation);
    }

    @Operation(summary = "Clock out for an employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @PostMapping("/clock-out")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceEvent clockOut(@RequestParam @NotNull Long employeeId,
                                    @RequestParam(required = false) String deviceInfo,
                                    @RequestParam(required = false) String geoLocation) {
        return attendanceService.clockOut(employeeId, deviceInfo, geoLocation);
    }
}