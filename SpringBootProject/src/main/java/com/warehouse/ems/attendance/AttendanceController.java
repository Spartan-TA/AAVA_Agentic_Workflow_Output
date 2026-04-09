package com.warehouse.ems.attendance;

import com.warehouse.ems.attendance.dto.AttendanceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for attendance clock-in/out.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Time & Attendance Management")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    @Operation(summary = "Clock in")
    public ResponseEntity<AttendanceDto> clockIn(
            @RequestParam @NotNull Long employeeId,
            @RequestParam @NotBlank String deviceId,
            @RequestParam @NotBlank String location) {
        return new ResponseEntity<>(
                AttendanceDto.fromEntity(attendanceService.clockIn(employeeId, deviceId, location)),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    @Operation(summary = "Clock out")
    public ResponseEntity<AttendanceDto> clockOut(
            @RequestParam @NotNull Long employeeId,
            @RequestParam @NotBlank String deviceId,
            @RequestParam @NotBlank String location) {
        return new ResponseEntity<>(
                AttendanceDto.fromEntity(attendanceService.clockOut(employeeId, deviceId, location)),
                HttpStatus.CREATED
        );
    }
}
