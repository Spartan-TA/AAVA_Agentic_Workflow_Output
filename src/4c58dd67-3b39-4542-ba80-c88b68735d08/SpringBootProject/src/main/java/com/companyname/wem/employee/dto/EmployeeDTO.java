package com.companyname.wem.employee.dto;

import com.companyname.wem.employee.domain.Role;
import com.companyname.wem.employee.domain.Status;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Shift group is required")
    private String shiftGroup;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotNull(message = "Status is required")
    private Status status;
}
