package com.wms.ems.attendance.controller;

import com.wms.ems.attendance.dto.AttendanceDto;
import com.wms.ems.attendance.dto.CorrectionDto;
import com.wms.ems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Endpoints for employee attendance management")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Operation(summary = "Clock in for attendance")
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> clockIn(@Valid @RequestBody AttendanceDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(attendanceService.clockIn(dto, principal.getName()));
    }

    @Operation(summary = "Clock out for attendance")
    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> clockOut(@Valid @RequestBody AttendanceDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(attendanceService.clockOut(dto, principal.getName()));
    }

    @Operation(summary = "Submit attendance correction request")
    @PostMapping("/corrections")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> submitCorrection(@Valid @RequestBody CorrectionDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(attendanceService.submitCorrection(dto, principal.getName()));
    }

    @Operation(summary = "Get attendance reports")
    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<AttendanceDto>> getReports(@RequestParam(required = false) String employeeId,
                                                         @RequestParam(required = false) String fromDate,
                                                         @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(attendanceService.getReports(employeeId, fromDate, toDate));
    }
}
