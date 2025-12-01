package com.warehouse.ems.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for Attendance operations.
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<AttendanceEvent> clockIn(@RequestParam Long employeeId, @RequestParam Long shiftId) {
        AttendanceEvent event = attendanceService.clockIn(employeeId, shiftId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<AttendanceEvent> clockOut(@RequestParam Long eventId) {
        AttendanceEvent event = attendanceService.clockOut(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceReport(@RequestParam Long employeeId) {
        List<AttendanceEvent> report = attendanceService.getAttendanceReport(employeeId);
        return ResponseEntity.ok(report);
    }
}
