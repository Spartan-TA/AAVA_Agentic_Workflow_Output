package com.company.wms.attendance.controller;

import com.company.wms.attendance.entity.AttendanceEvent;
import com.company.wms.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for attendance operations.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@RequestParam Long employeeId,
                                                   @RequestParam String deviceId,
                                                   @RequestParam String location,
                                                   @RequestParam Long shiftId) {
        AttendanceEvent event = attendanceService.clockIn(employeeId, deviceId, location, shiftId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@RequestParam Long employeeId,
                                                    @RequestParam String deviceId,
                                                    @RequestParam String location,
                                                    @RequestParam Long shiftId,
                                                    @RequestParam Double hoursWorked) {
        AttendanceEvent event = attendanceService.clockOut(employeeId, deviceId, location, shiftId, hoursWorked);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceEventsForEmployee(@PathVariable Long employeeId) {
        List<AttendanceEvent> events = attendanceService.getAttendanceEventsForEmployee(employeeId);
        return ResponseEntity.ok(events);
    }
}
