package com.warehouse.ems.scheduling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "DTO for Schedule entity")
public class ScheduleDTO {
    @Schema(description = "Schedule ID")
    private Long id;

    @Schema(description = "Employee ID")
    private Long employeeId;

    @Schema(description = "Shift template ID")
    private Long shiftId;

    @NotNull
    @Schema(description = "Date of the schedule")
    private LocalDate date;

    @Schema(description = "Schedule status (ASSIGNED, CONFLICT, COMPLETED)")
    private String status;

    @Schema(description = "Conflict description if any")
    private String conflict;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConflict() { return conflict; }
    public void setConflict(String conflict) { this.conflict = conflict; }
}
