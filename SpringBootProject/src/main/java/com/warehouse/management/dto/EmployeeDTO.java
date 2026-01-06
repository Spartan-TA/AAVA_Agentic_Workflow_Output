package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "^\+?[0-9]{7,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank
    @Size(max = 100)
    private String jobTitle;

    @NotBlank
    @Size(max = 100)
    private String department;

    @NotNull
    private Boolean active;
}