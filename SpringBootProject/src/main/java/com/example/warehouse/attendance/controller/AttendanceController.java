package com.example.warehouse.attendance.controller;

import com.example.warehouse.attendance.entity.AttendanceEvent;
import com.example.warehouse.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    // Get all attendance events
    @GetMapping
    public List<AttendanceEvent> getAllEvents() {
        return attendanceService.getAllEvents();
    }

    // Get attendance events for an employee
    @GetMapping("/employee/{employeeId}")
    public List<AttendanceEvent> getEventsByEmployee(@PathVariable Long employeeId) {
        return attendanceService.getEventsByEmployee(employeeId);
    }

    // Get attendance events for an employee between two dates
    @GetMapping("/employee/{employeeId}/range")
    public List<AttendanceEvent> getEventsByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return attendanceService.getEventsByEmployeeAndDateRange(employeeId, start, end);
    }

    // Get event by ID
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceEvent> getEventById(@PathVariable Long id) {
        Optional<AttendanceEvent> event = attendanceService.getEventById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create new attendance event
    @PostMapping
    public ResponseEntity<AttendanceEvent> createEvent(@RequestBody AttendanceEvent event) {
        AttendanceEvent created = attendanceService.createEvent(event);
        return ResponseEntity.ok(created);
    }

    // Delete attendance event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        boolean deleted = attendanceService.deleteEvent(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
