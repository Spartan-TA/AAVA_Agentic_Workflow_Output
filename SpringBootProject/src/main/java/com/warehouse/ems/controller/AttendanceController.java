package com.warehouse.ems.controller;

import com.warehouse.ems.domain.AttendanceRecord;
import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.service.AttendanceService;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for Attendance management.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Clock in endpoint.
     */
    @PostMapping("/clock-in/{employeeId}")
    @PreAuthorize("hasAnyRole('WORKER','SUPERVISOR','HR','ADMIN')")
    public ResponseEntity<AttendanceRecord> clockIn(@PathVariable Long employeeId,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime clockInTime) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            AttendanceRecord record = attendanceService.clockIn(employeeOpt.get(), clockInTime);
            return new ResponseEntity<>(record, HttpStatus.CREATED);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Clock out endpoint.
     */
    @PostMapping("/clock-out/{recordId}")
    @PreAuthorize("hasAnyRole('WORKER','SUPERVISOR','HR','ADMIN')")
    public ResponseEntity<AttendanceRecord> clockOut(@PathVariable Long recordId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime clockOutTime) {
        AttendanceRecord record = attendanceService.clockOut(recordId, clockOutTime);
        return ResponseEntity.ok(record);
    }

    /**
     * Request correction for attendance record.
     */
    @PostMapping("/correction/{recordId}")
    @PreAuthorize("hasAnyRole('WORKER','SUPERVISOR','HR','ADMIN')")
    public ResponseEntity<Void> requestCorrection(@PathVariable Long recordId) {
        attendanceService.requestCorrection(recordId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Export attendance records for employee.
     */
    @GetMapping("/export/{employeeId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AttendanceRecord>> exportAttendance(@PathVariable Long employeeId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            List<AttendanceRecord> records = attendanceService.exportAttendance(employeeOpt.get(), start, end);
            return ResponseEntity.ok(records);
        }
        return ResponseEntity.notFound().build();
    }
}
