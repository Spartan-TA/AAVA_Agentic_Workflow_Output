package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance Response DTO")
public class AttendanceResponseDTO {
    @Schema(description = "Attendance ID", example = "1001")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Employee ID", example = "1")
    @JsonProperty("employeeId")
    private Long employeeId;

    @Schema(description = "Clock in time", example = "2024-06-01T08:00:00")
    @JsonProperty("clockInTime")
    private LocalDateTime clockInTime;

    @Schema(description = "Clock out time", example = "2024-06-01T17:00:00")
    @JsonProperty("clockOutTime")
    private LocalDateTime clockOutTime;

    @Schema(description = "Shift code", example = "SHIFT_A")
    @JsonProperty("shiftCode")
    private String shiftCode;

    @Schema(description = "Status", example = "PRESENT")
    @JsonProperty("status")
    private String status;
}