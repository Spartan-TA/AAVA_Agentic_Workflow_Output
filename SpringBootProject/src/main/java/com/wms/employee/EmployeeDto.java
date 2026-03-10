package com.wms.employee;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;
}
