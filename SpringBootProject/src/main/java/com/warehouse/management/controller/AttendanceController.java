package com.warehouse.management.controller;

import com.warehouse.management.dto.AttendanceClockInRequest;
import com.warehouse.management.dto.AttendanceClockOutRequest;
import com.warehouse.management.dto.AttendanceReportResponse;
import com.warehouse.management.service.AttendanceService;
import com.warehouse.management.util.FileExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance Management")
@Validated
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Operation(summary = "Clock in for an employee")
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@Valid @RequestBody AttendanceClockInRequest request) {
        attendanceService.clockIn(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Clock out for an employee")
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@Valid @RequestBody AttendanceClockOutRequest request) {
        attendanceService.clockOut(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get attendance reports with optional date filters and CSV export")
    @GetMapping("/reports")
    public ResponseEntity<?> getAttendanceReports(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean exportCsv
    ) {
        List<AttendanceReportResponse> reports = attendanceService.getAttendanceReports(employeeId, startDate, endDate);
        if (exportCsv) {
            String[] headers = {"Employee ID", "Name", "Clock In", "Clock Out", "Duration (min)", "Device", "Location"};
            List<java.util.Map<String, Object>> data = attendanceService.mapReportsToCsv(reports);
            byte[] csvBytes = FileExportUtil.generateCSV(data, headers);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csvBytes));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        }
        return ResponseEntity.ok(reports);
    }
}
