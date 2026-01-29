package com.warehouseems.employee.dto;

import com.warehouseems.employee.Role;
import com.warehouseems.employee.Status;
import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String badgeId;

    @NotNull
    private Role role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @NotNull
    private Status status;
}
