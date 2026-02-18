package com.companyname.wem.attendance.controller;

import com.companyname.wem.attendance.domain.AttendanceEvent;
import com.companyname.wem.attendance.dto.ClockEventDTO;
import com.companyname.wem.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody ClockEventDTO dto) {
        return attendanceService.clockIn(dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody ClockEventDTO dto) {
        return attendanceService.clockOut(dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceReport(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<AttendanceEvent> report = attendanceService.getAttendanceReport(employeeId, start, end);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/corrections")
    public ResponseEntity<AttendanceEvent> submitCorrection(@Valid @RequestBody ClockEventDTO dto) {
        return attendanceService.submitCorrection(dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
