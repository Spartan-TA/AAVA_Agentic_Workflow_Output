package com.wms;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Attendance endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<AttendanceDto> getAllEvents() {
        return attendanceService.getAllEvents().stream()
                .map(AttendanceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceDto> getEventsByEmployeeId(@PathVariable Long employeeId) {
        return attendanceService.getEventsByEmployeeId(employeeId).stream()
                .map(AttendanceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDto> getEventById(@PathVariable Long id) {
        Optional<AttendanceEvent> event = attendanceService.getEventById(id);
        return event.map(value -> ResponseEntity.ok(AttendanceDto.fromEntity(value)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AttendanceDto> createEvent(@Valid @RequestBody AttendanceDto dto) {
        AttendanceEvent event = attendanceService.createEvent(dto.toEntity());
        return ResponseEntity.ok(AttendanceDto.fromEntity(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceDto> updateEvent(@PathVariable Long id, @Valid @RequestBody AttendanceDto dto) {
        AttendanceEvent updated = attendanceService.updateEvent(id, dto.toEntity());
        return ResponseEntity.ok(AttendanceDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        attendanceService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
