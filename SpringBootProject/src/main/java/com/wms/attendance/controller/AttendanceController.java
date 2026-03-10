package com.wms.attendance.controller;

import com.wms.attendance.entity.AttendanceEvent;
import com.wms.attendance.service.AttendanceService;
import com.wms.attendance.dto.ClockInDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
     * Clock in endpoint.
     * @param dto ClockInDto
     * @return AttendanceEvent
     */
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEvent> clockIn(@RequestBody ClockInDto dto) {
        AttendanceEvent event = attendanceService.clockIn(dto.getEmployeeId(), dto.getLocation());
        return ResponseEntity.ok(event);
    }

    /**
     * Clock out endpoint.
     * @param dto ClockInDto
     * @return AttendanceEvent
     */
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEvent> clockOut(@RequestBody ClockInDto dto) {
        AttendanceEvent event = attendanceService.clockOut(dto.getEmployeeId(), dto.getLocation());
        return ResponseEntity.ok(event);
    }

    /**
     * Get daily total hours for an employee.
     * @param employeeId Employee ID
     * @param date Date
     * @return Total hours
     */
    @GetMapping("/daily-total")
    public ResponseEntity<Double> getDailyTotal(@RequestParam Long employeeId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        double total = attendanceService.calculateDailyTotals(employeeId, date);
        return ResponseEntity.ok(total);
    }
}
