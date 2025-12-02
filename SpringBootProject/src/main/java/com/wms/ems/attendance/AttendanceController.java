package com.wms.ems.attendance;

import com.wms.ems.attendance.dto.ClockEventDto;
import com.wms.ems.attendance.dto.CorrectionDto;
import com.wms.ems.attendance.dto.AttendanceReportDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) {
        // Geofence validation logic can be added here
        return ResponseEntity.ok(attendanceService.clockIn(dto.getEmployeeId(), dto.getDeviceId(), dto.getLocation(), dto.getShiftId()));
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) {
        return ResponseEntity.ok(attendanceService.clockOut(dto.getEmployeeId(), dto.getDeviceId(), dto.getLocation(), dto.getShiftId()));
    }

    @PostMapping("/corrections")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<?> submitCorrection(@RequestBody CorrectionDto dto) {
        // Correction workflow logic to be implemented
        return ResponseEntity.ok("Correction submitted for approval");
    }

    @GetMapping("/report/{employeeId}/{shiftId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<AttendanceReportDto> getAttendanceReport(@PathVariable Long employeeId, @PathVariable Long shiftId) {
        double hours = attendanceService.calculateHours(employeeId, shiftId);
        AttendanceReportDto report = new AttendanceReportDto();
        report.setEmployeeId(employeeId);
        report.setTotalHours(hours);
        // Missed punches logic to be implemented
        report.setMissedPunches(0);
        return ResponseEntity.ok(report);
    }
}
