package com.wms.employee.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Badge ID is required")
    @Size(max = 50)
    private String badgeId;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Department is required")
    private String department;

    private String shiftGroup;

    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    private String status;
}
