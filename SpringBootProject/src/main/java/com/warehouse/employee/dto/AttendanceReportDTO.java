package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance Report DTO")
public class AttendanceReportDTO {
    @Schema(description = "Employee ID", example = "1")
    @JsonProperty("employeeId")
    private Long employeeId;

    @Schema(description = "Employee name", example = "John Doe")
    @JsonProperty("employeeName")
    private String employeeName;

    @Schema(description = "Date", example = "2024-06-01")
    @JsonProperty("date")
    private LocalDate date;

    @Schema(description = "Clock in time", example = "08:00:00")
    @JsonProperty("clockInTime")
    private String clockInTime;

    @Schema(description = "Clock out time", example = "17:00:00")
    @JsonProperty("clockOutTime")
    private String clockOutTime;

    @Schema(description = "Status", example = "PRESENT")
    @JsonProperty("status")
    private String status;
}