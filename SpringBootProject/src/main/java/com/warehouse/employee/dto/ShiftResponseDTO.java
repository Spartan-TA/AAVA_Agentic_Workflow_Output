package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shift Response DTO")
public class ShiftResponseDTO {
    @Schema(description = "Shift ID", example = "10")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Shift code", example = "SHIFT_A")
    @JsonProperty("shiftCode")
    private String shiftCode;

    @Schema(description = "Shift name", example = "Morning Shift")
    @JsonProperty("shiftName")
    private String shiftName;

    @Schema(description = "Start time", example = "08:00:00")
    @JsonProperty("startTime")
    private LocalTime startTime;

    @Schema(description = "End time", example = "17:00:00")
    @JsonProperty("endTime")
    private LocalTime endTime;

    @Schema(description = "Is active", example = "true")
    @JsonProperty("active")
    private boolean active;
}