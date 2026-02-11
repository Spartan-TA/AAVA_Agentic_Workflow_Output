package com.company.project.controller;

import com.company.project.dto.ClockInRequest;
import com.company.project.dto.AttendanceResponse;
import com.company.project.service.AttendanceService;
import com.company.project.mapper.AttendanceMapper;
import com.company.project.exception.AttendanceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@Tag(name = "Attendance Management", description = "Clock-in/out and attendance records")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, AttendanceMapper attendanceMapper) {
        this.attendanceService = attendanceService;
        this.attendanceMapper = attendanceMapper;
    }

    @Operation(summary = "Clock in", responses = {
            @ApiResponse(responseCode = "201", description = "Clock-in successful"),
            @ApiResponse(responseCode = "400", description = "Invalid clock-in request")
    })
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody ClockInRequest request) {
        try {
            var attendance = attendanceService.clockIn(request);
            return ResponseEntity.status(201).body(attendanceMapper.toResponse(attendance));
        } catch (AttendanceException e) {
            throw e;
        }
    }

    @Operation(summary = "Clock out", responses = {
            @ApiResponse(responseCode = "200", description = "Clock-out successful"),
            @ApiResponse(responseCode = "400", description = "Invalid clock-out request")
    })
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(@Valid @RequestBody ClockInRequest request) {
        try {
            var attendance = attendanceService.clockOut(request);
            return ResponseEntity.ok(attendanceMapper.toResponse(attendance));
        } catch (AttendanceException e) {
            throw e;
        }
    }

    @Operation(summary = "Get attendance records for employee", responses = {
            @ApiResponse(responseCode = "200", description = "Attendance records returned")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        var records = attendanceService.getAttendanceByEmployee(employeeId);
        return ResponseEntity.ok(attendanceMapper.toResponseList(records));
    }
}
