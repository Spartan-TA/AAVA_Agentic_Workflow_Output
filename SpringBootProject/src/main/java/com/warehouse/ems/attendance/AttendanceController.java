package com.warehouse.ems.attendance;

import com.warehouse.ems.common.ApiResponse;
import com.warehouse.ems.common.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for Attendance endpoints: clock-in, clock-out, reports.
 */
@RestController
@RequestMapping("/attendance")
@Validated
public class AttendanceController {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * POST /attendance/clock-in
     * @param employeeId Employee ID
     * @return ApiResponse with AttendanceEvent
     */
    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceEvent>> clockIn(@RequestParam @NotNull Long employeeId) {
        try {
            AttendanceEvent event = attendanceService.clockIn(employeeId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Clock-in successful", event));
        } catch (IllegalStateException ex) {
            logger.warn("Clock-in failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }

    /**
     * POST /attendance/clock-out
     * @param employeeId Employee ID
     * @return ApiResponse with AttendanceEvent
     */
    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceEvent>> clockOut(@RequestParam @NotNull Long employeeId) {
        try {
            AttendanceEvent event = attendanceService.clockOut(employeeId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Clock-out successful", event));
        } catch (ResourceNotFoundException ex) {
            logger.warn("Clock-out failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, ex.getMessage(), null));
        } catch (IllegalStateException ex) {
            logger.warn("Clock-out failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }

    /**
     * GET /attendance/report
     * @param employeeId Employee ID
     * @param start Start date
     * @param end End date
     * @return ApiResponse with list of AttendanceEvent
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<AttendanceEvent>>> getAttendanceReport(
            @RequestParam @NotNull Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<AttendanceEvent> events = attendanceService.getAttendanceReport(employeeId, start, end);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance report fetched", events));
    }

    /**
     * POST /attendance/correction
     * @param correctionDto Correction DTO
     * @return ApiResponse with updated AttendanceEvent
     */
    @PostMapping("/correction")
    public ResponseEntity<ApiResponse<AttendanceEvent>> submitCorrection(@Valid @RequestBody AttendanceCorrectionDto correctionDto) {
        try {
            AttendanceEvent event = attendanceService.submitCorrection(
                    correctionDto.getEventId(),
                    correctionDto.getNewClockIn(),
                    correctionDto.getNewClockOut()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Correction submitted", event));
        } catch (ResourceNotFoundException ex) {
            logger.warn("Correction failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }

    /**
     * POST /attendance/correction/approve
     * @param eventId AttendanceEvent ID
     * @return ApiResponse with updated AttendanceEvent
     */
    @PostMapping("/correction/approve")
    public ResponseEntity<ApiResponse<AttendanceEvent>> approveCorrection(@RequestParam @NotNull Long eventId) {
        try {
            AttendanceEvent event = attendanceService.approveCorrection(eventId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Correction approved", event));
        } catch (ResourceNotFoundException ex) {
            logger.warn("Correction approval failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }
}

/**
 * DTO for attendance correction requests.
 */
class AttendanceCorrectionDto {
    @NotNull
    private Long eventId;
    @NotNull
    private LocalDateTime newClockIn;
    @NotNull
    private LocalDateTime newClockOut;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public LocalDateTime getNewClockIn() { return newClockIn; }
    public void setNewClockIn(LocalDateTime newClockIn) { this.newClockIn = newClockIn; }
    public LocalDateTime getNewClockOut() { return newClockOut; }
    public void setNewClockOut(LocalDateTime newClockOut) { this.newClockOut = newClockOut; }
}
