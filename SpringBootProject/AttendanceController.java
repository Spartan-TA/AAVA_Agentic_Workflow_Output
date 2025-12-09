package com.example.warehousemanagement.controller;

import com.example.warehousemanagement.dto.AttendanceEventDTO;
import com.example.warehousemanagement.entity.AttendanceEvent;
import com.example.warehousemanagement.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Attendance management.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceEventDTO>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        List<AttendanceEventDTO> events = attendanceService.getAttendanceEventsByEmployee(employeeId)
                .stream().map(AttendanceEventDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceEventDTO>> getAttendanceByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<AttendanceEventDTO> events = attendanceService.getAttendanceEventsByDate(localDate)
                .stream().map(AttendanceEventDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceEventDTO> getAttendanceEventById(@PathVariable Long id) {
        AttendanceEvent event = attendanceService.getAttendanceEventById(id);
        return ResponseEntity.ok(AttendanceEventDTO.fromEntity(event));
    }

    @PostMapping
    public ResponseEntity<AttendanceEventDTO> createAttendanceEvent(@RequestBody AttendanceEventDTO eventDTO) {
        AttendanceEvent created = attendanceService.createAttendanceEvent(eventDTO.toEntity());
        return new ResponseEntity<>(AttendanceEventDTO.fromEntity(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceEventDTO> updateAttendanceEvent(@PathVariable Long id, @RequestBody AttendanceEventDTO eventDTO) {
        AttendanceEvent updated = attendanceService.updateAttendanceEvent(id, eventDTO.toEntity());
        return ResponseEntity.ok(AttendanceEventDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendanceEvent(@PathVariable Long id) {
        attendanceService.deleteAttendanceEvent(id);
        return ResponseEntity.noContent().build();
    }
}
