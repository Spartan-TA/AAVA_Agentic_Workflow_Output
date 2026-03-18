package com.warehouse.ems.attendance.controller;

import com.warehouse.ems.attendance.dto.ClockInRequest;
import com.warehouse.ems.attendance.dto.ClockOutRequest;
import com.warehouse.ems.attendance.dto.AttendanceDto;
import com.warehouse.ems.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Attendance operations.
 */
@RestController
@RequestMapping("/api/attendance")
@Validated
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Clock in an employee.
     * @param request ClockInRequest DTO
     * @return ResponseEntity with AttendanceDto
     */
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<AttendanceDto> clockIn(@Valid @RequestBody ClockInRequest request) {
        AttendanceDto attendance = attendanceService.clockIn(request);
        return new ResponseEntity<>(attendance, HttpStatus.CREATED);
    }

    /**
     * Clock out an employee.
     * @param request ClockOutRequest DTO
     * @return ResponseEntity with AttendanceDto
     */
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<AttendanceDto> clockOut(@Valid @RequestBody ClockOutRequest request) {
        AttendanceDto attendance = attendanceService.clockOut(request);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Get attendance events for an employee.
     * @param employeeId Employee ID
     * @return ResponseEntity with list of AttendanceDto
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByEmployee(employeeId);
        return ResponseEntity.ok(attendanceList);
    }

    /**
     * Get attendance events for an employee within a date range.
     * @param employeeId Employee ID
     * @param start Start date/time
     * @param end End date/time
     * @return ResponseEntity with list of AttendanceDto
     */
    @GetMapping("/employee/{employeeId}/range")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam("start") LocalDateTime start,
            @RequestParam("end") LocalDateTime end) {
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByEmployeeAndDateRange(employeeId, start, end);
        return ResponseEntity.ok(attendanceList);
    }
}
