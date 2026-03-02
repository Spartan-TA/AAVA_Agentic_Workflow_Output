package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee Search DTO")
public class EmployeeSearchDTO {
    @Schema(description = "Employee full name", example = "John Doe")
    @Size(max = 100)
    @JsonProperty("fullName")
    private String fullName;

    @Schema(description = "Department name", example = "Logistics")
    @Size(max = 50)
    @JsonProperty("department")
    private String department;

    @Schema(description = "Role", example = "WORKER")
    @Size(max = 30)
    @JsonProperty("role")
    private String role;

    @Schema(description = "Badge ID", example = "BADGE1234")
    @Size(max = 20)
    @JsonProperty("badgeId")
    private String badgeId;
}