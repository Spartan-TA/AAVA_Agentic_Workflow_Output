package com.wms.ems.report.controller;

import com.wms.ems.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for various reports")
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "Get attendance report")
    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> getAttendanceReport(@RequestParam(required = false) String employeeId,
                                                 @RequestParam(required = false) String fromDate,
                                                 @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(reportService.getAttendanceReport(employeeId, fromDate, toDate));
    }

    @Operation(summary = "Get overtime report")
    @GetMapping("/overtime")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> getOvertimeReport(@RequestParam(required = false) String employeeId,
                                               @RequestParam(required = false) String fromDate,
                                               @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(reportService.getOvertimeReport(employeeId, fromDate, toDate));
    }

    @Operation(summary = "Get leave report")
    @GetMapping("/leave")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> getLeaveReport(@RequestParam(required = false) String employeeId,
                                            @RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(reportService.getLeaveReport(employeeId, fromDate, toDate));
    }

    @Operation(summary = "Get certifications report")
    @GetMapping("/certifications")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> getCertificationsReport(@RequestParam(required = false) String employeeId) {
        return ResponseEntity.ok(reportService.getCertificationsReport(employeeId));
    }

    @Operation(summary = "Get safety report")
    @GetMapping("/safety")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> getSafetyReport(@RequestParam(required = false) String fromDate,
                                             @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(reportService.getSafetyReport(fromDate, toDate));
    }
}
