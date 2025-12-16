package com.companyname.wems.attendance.controller;

import com.companyname.wems.attendance.model.AttendanceEvent;
import com.companyname.wems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AttendanceController for Time & Attendance (E04)
 * Exposes REST endpoints for clock-in/out and attendance retrieval
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @Operation(summary = "Clock-in for an employee", responses = {
            @ApiResponse(responseCode = "201", description = "Clock-in event created")
    })
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@RequestParam Long employeeId,
                                                   @RequestParam(required = false) String location,
                                                   @RequestParam(required = false) String deviceInfo) {
        AttendanceEvent event = attendanceService.clockIn(employeeId, location, deviceInfo);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    @Operation(summary = "Clock-out for an employee", responses = {
            @ApiResponse(responseCode = "201", description = "Clock-out event created")
    })
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@RequestParam Long employeeId,
                                                    @RequestParam(required = false) String location,
                                                    @RequestParam(required = false) String deviceInfo) {
        AttendanceEvent event = attendanceService.clockOut(employeeId, location, deviceInfo);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    @Operation(summary = "Get attendance events for an employee", responses = {
            @ApiResponse(responseCode = "200", description = "Attendance events returned")
    })
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<AttendanceEvent>> getAttendanceForEmployee(@PathVariable Long id) {
        List<AttendanceEvent> events = attendanceService.getEventsForEmployee(id);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Add missed punch correction event", responses = {
            @ApiResponse(responseCode = "201", description = "Correction event added")
    })
    @PostMapping("/correction")
    public ResponseEntity<AttendanceEvent> addCorrectionEvent(@RequestParam Long employeeId,
                                                             @RequestParam String eventType,
                                                             @RequestParam String timestamp,
                                                             @RequestParam(required = false) String location,
                                                             @RequestParam(required = false) String deviceInfo) {
        AttendanceEvent event = attendanceService.addCorrectionEvent(
                employeeId, eventType, LocalDateTime.parse(timestamp), location, deviceInfo);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }
}
