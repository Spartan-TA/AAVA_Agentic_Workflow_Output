package com.wms.ems.attendance.controller;

import com.wms.ems.attendance.model.AttendanceEvent;
import com.wms.ems.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestParam Long employeeId,
                                     @RequestParam Long shiftId,
                                     @RequestParam String deviceId,
                                     @RequestParam String location,
                                     @RequestParam Double latitude,
                                     @RequestParam Double longitude) {
        try {
            AttendanceEvent event = attendanceService.clockIn(employeeId, shiftId, deviceId, location, latitude, longitude);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestParam Long employeeId,
                                      @RequestParam Long shiftId,
                                      @RequestParam String deviceId,
                                      @RequestParam String location,
                                      @RequestParam Double latitude,
                                      @RequestParam Double longitude) {
        try {
            AttendanceEvent event = attendanceService.clockOut(employeeId, shiftId, deviceId, location, latitude, longitude);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/correction")
    public ResponseEntity<?> requestCorrection(@RequestParam Long attendanceId,
                                               @RequestParam String reason) {
        try {
            AttendanceEvent event = attendanceService.requestCorrection(attendanceId, reason);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceForEmployee(@PathVariable Long employeeId,
                                                                         @RequestParam LocalDateTime start,
                                                                         @RequestParam LocalDateTime end) {
        List<AttendanceEvent> events = attendanceService.getAttendanceForEmployee(employeeId, start, end);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/hours-worked/{employeeId}")
    public ResponseEntity<Double> getHoursWorked(@PathVariable Long employeeId,
                                                 @RequestParam LocalDateTime start,
                                                 @RequestParam LocalDateTime end) {
        double hours = attendanceService.calculateHoursWorked(employeeId, start, end);
        return ResponseEntity.ok(hours);
    }
}