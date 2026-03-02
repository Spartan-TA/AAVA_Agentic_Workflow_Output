package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee Response DTO")
public class EmployeeResponseDTO {
    @Schema(description = "Employee ID", example = "1")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Employee full name", example = "John Doe")
    @NotBlank
    @Size(max = 100)
    @JsonProperty("fullName")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@warehouse.com")
    @Email
    @NotBlank
    @JsonProperty("email")
    private String email;

    @Schema(description = "Badge ID", example = "BADGE1234")
    @NotBlank
    @Size(max = 20)
    @JsonProperty("badgeId")
    private String badgeId;

    @Schema(description = "Department name", example = "Logistics")
    @NotBlank
    @Size(max = 50)
    @JsonProperty("department")
    private String department;

    @Schema(description = "Role", example = "WORKER")
    @NotBlank
    @Size(max = 30)
    @JsonProperty("role")
    private String role;

    @Schema(description = "Date of joining", example = "2023-01-15")
    @NotNull
    @JsonProperty("dateOfJoining")
    private LocalDate dateOfJoining;

    @Schema(description = "Active status", example = "true")
    @JsonProperty("active")
    private boolean active;
}