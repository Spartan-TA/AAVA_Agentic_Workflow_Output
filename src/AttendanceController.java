package com.company.wms.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for attendance endpoints.
 */
@RestController
@RequestMapping("/attendance")
@Validated
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    /**
     * POST /attendance/clock-in
     */
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttendanceDTO> clockIn(@Valid @RequestBody ClockInRequest request) {
        return ResponseEntity.ok(attendanceService.clockIn(request));
    }

    /**
     * POST /attendance/clock-out
     */
    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttendanceDTO> clockOut(@Valid @RequestBody ClockOutRequest request) {
        return ResponseEntity.ok(attendanceService.clockOut(request));
    }

    /**
     * POST /attendance/corrections
     */
    @PostMapping("/corrections")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<CorrectionRequest> requestCorrection(@RequestParam Long employeeId, @RequestParam String originalTimestamp, @RequestParam String reason) {
        LocalDateTime timestamp = LocalDateTime.parse(originalTimestamp);
        return ResponseEntity.ok(attendanceService.requestCorrection(employeeId, timestamp, reason));
    }

    /**
     * GET /attendance/reports
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('MANAGER') or #employeeId == authentication.principal.id")
    public ResponseEntity<List<AttendanceDTO>> getDailyReport(@RequestParam Long employeeId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(attendanceService.calculateDailyTotals(employeeId, localDate));
    }
}
