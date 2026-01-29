package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.AttendanceDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/attendance")
@Validated
public class AttendanceController {
    private final List<AttendanceDto> attendanceRecords = new ArrayList<>();

    @PreAuthorize("hasAuthority('ATTENDANCE_CLOCK_IN')")
    @PostMapping("/clock-in")
    public AttendanceDto clockIn(@Valid @RequestBody AttendanceDto attendanceDto) {
        attendanceDto.setClockOut(null);
        attendanceRecords.add(attendanceDto);
        return attendanceDto;
    }

    @PreAuthorize("hasAuthority('ATTENDANCE_CLOCK_OUT')")
    @PostMapping("/clock-out/{index}")
    public AttendanceDto clockOut(@PathVariable int index) {
        if (index < 0 || index >= attendanceRecords.size()) throw new IllegalArgumentException("Invalid index");
        AttendanceDto record = attendanceRecords.get(index);
        record.setClockOut(java.time.LocalDateTime.now());
        return record;
    }

    @PreAuthorize("hasAuthority('ATTENDANCE_CORRECT')")
    @PutMapping("/corrections/{index}")
    public AttendanceDto correctAttendance(@PathVariable int index, @Valid @RequestBody AttendanceDto attendanceDto) {
        if (index < 0 || index >= attendanceRecords.size()) throw new IllegalArgumentException("Invalid index");
        attendanceRecords.set(index, attendanceDto);
        return attendanceDto;
    }

    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    @GetMapping
    public List<AttendanceDto> getAttendanceRecords() {
        return Collections.unmodifiableList(attendanceRecords);
    }
}
