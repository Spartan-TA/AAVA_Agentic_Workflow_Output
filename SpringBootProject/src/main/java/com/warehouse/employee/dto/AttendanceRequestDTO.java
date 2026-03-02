package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance Request DTO")
public class AttendanceRequestDTO {
    @Schema(description = "Employee ID", example = "1")
    @NotNull
    @JsonProperty("employeeId")
    private Long employeeId;

    @Schema(description = "Clock in time", example = "2024-06-01T08:00:00")
    @NotNull
    @JsonProperty("clockInTime")
    private LocalDateTime clockInTime;

    @Schema(description = "Clock out time", example = "2024-06-01T17:00:00")
    @JsonProperty("clockOutTime")
    private LocalDateTime clockOutTime;

    @Schema(description = "Shift code", example = "SHIFT_A")
    @Size(max = 20)
    @JsonProperty("shiftCode")
    private String shiftCode;
}