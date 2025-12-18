package com.warehouse.ems.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @PreAuthorize("hasRole('WORKER') or hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    @PostMapping("/clock-in")
    public ResponseEntity<Attendance> clockIn(@RequestParam Long employeeId,
                                              @RequestParam Long shiftId,
                                              @RequestParam(required = false) String deviceInfo,
                                              @RequestParam(required = false) String geofenceLocation) {
        return new ResponseEntity<>(attendanceService.clockIn(employeeId, shiftId, deviceInfo, geofenceLocation), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('WORKER') or hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    @PostMapping("/clock-out")
    public ResponseEntity<Attendance> clockOut(@RequestParam Long attendanceId) {
        return ResponseEntity.ok(attendanceService.clockOut(attendanceId));
    }

    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendance(@PathVariable Long id) {
        Optional<Attendance> attendance = attendanceService.getAttendance(id);
        return attendance.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
