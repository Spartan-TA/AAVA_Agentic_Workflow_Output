package com.warehouse.ems.controller;

import com.warehouse.ems.dto.AttendanceDTO;
import com.warehouse.ems.dto.ClockInRequestDTO;
import com.warehouse.ems.dto.ClockOutRequestDTO;
import com.warehouse.ems.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AttendanceDTO> clockIn(@RequestBody ClockInRequestDTO request) {
        return ResponseEntity.ok(attendanceService.clockIn(request));
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AttendanceDTO> clockOut(@RequestBody ClockOutRequestDTO request) {
        return ResponseEntity.ok(attendanceService.clockOut(request));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('SUPERVISOR','HR','ADMIN')")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceReports(startDate, endDate));
    }
}
