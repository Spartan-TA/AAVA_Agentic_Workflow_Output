package com.warehouse.ems.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Set<String> roles;
    @NotBlank
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;
    @NotBlank
    private String password;
}
