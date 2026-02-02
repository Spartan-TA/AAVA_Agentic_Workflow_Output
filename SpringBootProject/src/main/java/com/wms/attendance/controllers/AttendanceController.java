package com.wms.attendance.controllers;

import com.wms.attendance.dtos.ClockEventDto;
import com.wms.attendance.services.AttendanceService;
import com.wms.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Attendance endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock")
    public ResponseEntity<ApiResponse<ClockEventDto>> clockEvent(@RequestBody ClockEventDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Clock event recorded successfully", attendanceService.clockEvent(dto)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<ClockEventDto>>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance events fetched successfully", attendanceService.getAttendanceByEmployee(employeeId)));
    }
}
