package com.company.wms.attendance.controller;

import com.company.wms.attendance.domain.AttendanceEvent;
import com.company.wms.attendance.service.AttendanceService;
import com.company.wms.common.dto.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for attendance operations.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Clock-in endpoint.
     */
    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceEvent>> clockIn(@RequestParam Long employeeId, @RequestParam(required = false) String notes) {
        AttendanceEvent event = attendanceService.clockIn(employeeId, notes);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    /**
     * Clock-out endpoint.
     */
    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceEvent>> clockOut(@RequestParam Long employeeId, @RequestParam(required = false) String notes) {
        AttendanceEvent event = attendanceService.clockOut(employeeId, notes);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    /**
     * Get all attendance events for employee.
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceEvent>>> getEventsForEmployee(@PathVariable Long employeeId) {
        List<AttendanceEvent> events = attendanceService.getEventsForEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get attendance events for employee in date range.
     */
    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<ApiResponse<List<AttendanceEvent>>> getEventsForEmployeeInRange(
            @PathVariable Long employeeId,
            @RequestParam @NotNull LocalDateTime start,
            @RequestParam @NotNull LocalDateTime end) {
        List<AttendanceEvent> events = attendanceService.getEventsForEmployeeInRange(employeeId, start, end);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get paginated attendance events for employee.
     */
    @GetMapping("/employee/{employeeId}/page")
    public ResponseEntity<ApiResponse<Page<AttendanceEvent>>> getEventsPage(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEvent> events = attendanceService.getEventsPage(employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get attendance event by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceEvent>> getById(@PathVariable Long id) {
        AttendanceEvent event = attendanceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
}
