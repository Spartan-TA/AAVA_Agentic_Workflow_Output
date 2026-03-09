package com.company.warehouse.attendance.controller;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import com.company.warehouse.attendance.service.AttendanceService;
import com.company.warehouse.attendance.service.GeofenceService;
import com.company.warehouse.attendance.dto.ClockInRequest;
import com.company.warehouse.attendance.dto.ClockOutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for Attendance events.
 */
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final GeofenceService geofenceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, GeofenceService geofenceService) {
        this.attendanceService = attendanceService;
        this.geofenceService = geofenceService;
    }

    @Operation(summary = "Clock in for an employee")
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody ClockInRequest request) {
        if (!geofenceService.isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(AttendanceEvent.EventType.CLOCK_IN);
        event.setEventTime(LocalDateTime.now());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setLocationDescription(request.getLocationDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.recordEvent(event));
    }

    @Operation(summary = "Clock out for an employee")
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody ClockOutRequest request) {
        if (!geofenceService.isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(AttendanceEvent.EventType.CLOCK_OUT);
        event.setEventTime(LocalDateTime.now());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setLocationDescription(request.getLocationDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.recordEvent(event));
    }

    @Operation(summary = "Get paginated attendance events for an employee")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<AttendanceEvent>> getEventsForEmployee(
            @PathVariable Long employeeId,
            Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getEventsForEmployee(employeeId, pageable));
    }

    @Operation(summary = "Get attendance events for an employee in a date range")
    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<List<AttendanceEvent>> getEventsForEmployeeInRange(
            @PathVariable Long employeeId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(attendanceService.getEventsForEmployeeInRange(employeeId, start, end));
    }

    @Operation(summary = "Get attendance event by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceEvent> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getEvent(id));
    }

    @Operation(summary = "Delete attendance event by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        attendanceService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
