package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule Request DTO")
public class ScheduleRequestDTO {
    @Schema(description = "Employee ID", example = "1")
    @NotNull
    @JsonProperty("employeeId")
    private Long employeeId;

    @Schema(description = "Shift code", example = "SHIFT_A")
    @NotNull
    @Size(max = 20)
    @JsonProperty("shiftCode")
    private String shiftCode;

    @Schema(description = "Schedule date", example = "2024-06-01")
    @NotNull
    @JsonProperty("date")
    private LocalDate date;
}