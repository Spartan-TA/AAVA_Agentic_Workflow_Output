package com.warehouse.ems.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @Operation(summary = "Clock-in for an employee", description = "Records clock-in event")
    @ApiResponse(responseCode = "200", description = "Clock-in successful")
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceDTO> clockIn(@RequestParam Long employeeId,
                                                 @RequestParam Long shiftId,
                                                 @RequestParam String deviceId,
                                                 @RequestParam String geolocation) {
        Attendance attendance = attendanceService.clockIn(employeeId, shiftId, deviceId, geolocation);
        AttendanceDTO dto = toDTO(attendance);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Clock-out for an employee", description = "Records clock-out event")
    @ApiResponse(responseCode = "200", description = "Clock-out successful")
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceDTO> clockOut(@RequestParam Long attendanceId) {
        Attendance attendance = attendanceService.clockOut(attendanceId);
        AttendanceDTO dto = toDTO(attendance);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get daily attendance totals", description = "Returns daily totals for all employees")
    @ApiResponse(responseCode = "200", description = "Daily totals fetched")
    @GetMapping("/daily-totals")
    public ResponseEntity<List<Object[]>> getDailyTotals(@RequestParam String date) {
        LocalDateTime localDate = LocalDateTime.parse(date);
        List<Object[]> totals = attendanceService.getDailyTotals(localDate);
        return ResponseEntity.ok(totals);
    }

    // Utility method to convert Attendance to DTO
    private AttendanceDTO toDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setEmployeeId(attendance.getEmployee().getId());
        dto.setShiftId(attendance.getShift() != null ? attendance.getShift().getId() : null);
        dto.setClockIn(attendance.getClockIn());
        dto.setClockOut(attendance.getClockOut());
        dto.setDeviceId(attendance.getDeviceId());
        dto.setGeolocation(attendance.getGeolocation());
        dto.setHoursWorked(attendance.getHoursWorked());
        return dto;
    }
}
