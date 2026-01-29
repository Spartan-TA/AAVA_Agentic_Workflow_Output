package com.company.wms.attendance.controller;

import com.company.wms.attendance.model.AttendanceRecord;
import com.company.wms.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for attendance management.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceService.getAllAttendanceRecords();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceRecord> getAttendanceRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceRecordById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRecord> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return attendanceService.getAttendanceByEmployee(employeeId);
    }

    @GetMapping("/date/{date}")
    public List<AttendanceRecord> getAttendanceByDate(@PathVariable LocalDate date) {
        return attendanceService.getAttendanceByDate(date);
    }

    @PostMapping
    public ResponseEntity<AttendanceRecord> createAttendanceRecord(@RequestBody AttendanceRecord record) {
        AttendanceRecord created = attendanceService.createAttendanceRecord(record);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceRecord> updateAttendanceRecord(@PathVariable Long id, @RequestBody AttendanceRecord record) {
        AttendanceRecord updated = attendanceService.updateAttendanceRecord(id, record);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendanceRecord(@PathVariable Long id) {
        attendanceService.deleteAttendanceRecord(id);
        return ResponseEntity.noContent().build();
    }
}
