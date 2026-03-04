package com.warehouse.ems.scheduling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/scheduling")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @Operation(summary = "Assign a schedule to an employee", description = "Assigns a shift to an employee for a specific date")
    @ApiResponse(responseCode = "200", description = "Schedule assigned successfully")
    @PostMapping("/assign")
    public ResponseEntity<ScheduleDTO> assignSchedule(@RequestParam Long employeeId,
                                                     @RequestParam Long shiftId,
                                                     @RequestParam String date) {
        Schedule schedule = scheduleService.assignSchedule(employeeId, shiftId, LocalDate.parse(date));
        ScheduleDTO dto = toDTO(schedule);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Bulk assign schedules", description = "Assigns a shift to multiple employees for a specific date")
    @ApiResponse(responseCode = "200", description = "Bulk assignment successful")
    @PostMapping("/bulk-assign")
    public ResponseEntity<List<ScheduleDTO>> bulkAssign(@RequestParam Set<Long> employeeIds,
                                                       @RequestParam Long shiftId,
                                                       @RequestParam String date) {
        List<Schedule> schedules = scheduleService.bulkAssignSchedules(employeeIds, shiftId, LocalDate.parse(date));
        List<ScheduleDTO> dtos = schedules.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Find conflicts by date", description = "Returns all schedule conflicts for a given date")
    @ApiResponse(responseCode = "200", description = "Conflicts fetched")
    @GetMapping("/conflicts")
    public ResponseEntity<List<ScheduleDTO>> findConflicts(@RequestParam String date) {
        List<Schedule> conflicts = scheduleService.findConflictsByDate(LocalDate.parse(date));
        List<ScheduleDTO> dtos = conflicts.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Update schedule status", description = "Updates the status and conflict info for a schedule")
    @ApiResponse(responseCode = "200", description = "Schedule updated")
    @PutMapping("/update-status")
    public ResponseEntity<ScheduleDTO> updateStatus(@RequestParam Long scheduleId,
                                                   @RequestParam String status,
                                                   @RequestParam(required = false) String conflict) {
        Schedule schedule = scheduleService.updateScheduleStatus(scheduleId, status, conflict);
        ScheduleDTO dto = toDTO(schedule);
        return ResponseEntity.ok(dto);
    }

    // Utility method to convert Schedule to DTO
    private ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setEmployeeId(schedule.getEmployee().getId());
        dto.setShiftId(schedule.getShift().getId());
        dto.setDate(schedule.getDate());
        dto.setStatus(schedule.getStatus());
        dto.setConflict(schedule.getConflict());
        return dto;
    }
}
