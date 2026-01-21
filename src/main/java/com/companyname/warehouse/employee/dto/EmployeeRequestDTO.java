package com.companyname.warehouse.employee.dto;

import com.companyname.warehouse.employee.model.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Employee.Role role;
    @NotBlank
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotNull
    private Employee.Status status;
}
