package com.warehouse.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {
    @Schema(description = "Badge ID", example = "EMP12345")
    @JsonProperty("badgeId")
    @NotBlank(message = "Badge ID is required")
    @Size(max = 20)
    private String badgeId;

    @Schema(description = "First Name", example = "John")
    @JsonProperty("firstName")
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @Schema(description = "Last Name", example = "Doe")
    @JsonProperty("lastName")
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @Schema(description = "Email", example = "john.doe@example.com")
    @JsonProperty("email")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Phone Number", example = "+1234567890")
    @JsonProperty("phoneNumber")
    @Pattern(regexp = "^\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @Schema(description = "Role", example = "WAREHOUSE_WORKER")
    @JsonProperty("role")
    @NotBlank(message = "Role is required")
    private String role;

    @Schema(description = "Department", example = "Logistics")
    @JsonProperty("department")
    @NotBlank(message = "Department is required")
    private String department;

    @Schema(description = "Shift Group", example = "A")
    @JsonProperty("shiftGroup")
    @NotBlank(message = "Shift group is required")
    private String shiftGroup;

    @Schema(description = "Hire Date", example = "2024-01-01")
    @JsonProperty("hireDate")
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @Schema(description = "Status", example = "ACTIVE")
    @JsonProperty("status")
    @NotBlank(message = "Status is required")
    private String status;
}
