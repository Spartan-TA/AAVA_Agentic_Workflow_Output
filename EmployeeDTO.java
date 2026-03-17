package com.wms.ems.employee.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Shift Group is required")
    private String shiftGroup;

    @NotNull(message = "Hire Date is required")
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    private String status;
}
