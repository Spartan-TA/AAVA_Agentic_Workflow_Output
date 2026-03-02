package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shift Request DTO")
public class ShiftRequestDTO {
    @Schema(description = "Shift code", example = "SHIFT_A")
    @NotBlank
    @Size(max = 20)
    @JsonProperty("shiftCode")
    private String shiftCode;

    @Schema(description = "Shift name", example = "Morning Shift")
    @NotBlank
    @Size(max = 50)
    @JsonProperty("shiftName")
    private String shiftName;

    @Schema(description = "Start time", example = "08:00:00")
    @NotNull
    @JsonProperty("startTime")
    private LocalTime startTime;

    @Schema(description = "End time", example = "17:00:00")
    @NotNull
    @JsonProperty("endTime")
    private LocalTime endTime;

    @Schema(description = "Is active", example = "true")
    @JsonProperty("active")
    private boolean active;
}