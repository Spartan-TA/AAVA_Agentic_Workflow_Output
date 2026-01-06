package com.companyname.warehouse.attendance.controller;

import com.companyname.warehouse.attendance.dto.AttendanceRequestDTO;
import com.companyname.warehouse.attendance.dto.AttendanceResponseDTO;
import com.companyname.warehouse.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for attendance management.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Clock-in/out and attendance queries")
@Validated
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Operation(summary = "Clock-in for an employee")
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponseDTO> clockIn(@Valid @RequestBody AttendanceRequestDTO dto) {
        return new ResponseEntity<>(attendanceService.clockIn(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Clock-out for an attendance record")
    @PutMapping("/clock-out/{attendanceId}")
    public ResponseEntity<AttendanceResponseDTO> clockOut(@PathVariable Long attendanceId, @Valid @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.clockOut(attendanceId, dto));
    }

    @Operation(summary = "Get attendance records for an employee (paginated)")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<AttendanceResponseDTO>> getEmployeeAttendance(@PathVariable Long employeeId, Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId, pageable));
    }

    @Operation(summary = "Get a single attendance record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDTO> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendance(id));
    }
}
