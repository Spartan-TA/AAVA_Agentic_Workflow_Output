package com.companyname.wems.employee.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for creating a new employee (E02)
 */
@Data
public class CreateEmployeeRequest {
    @NotNull
    @Size(min = 1, max = 20)
    private String badgeId;

    @NotNull
    @Size(min = 1, max = 50)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 50)
    private String lastName;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotNull
    @Size(max = 50)
    private String department;

    @NotNull
    @Size(max = 50)
    private String position;

    @NotNull
    private LocalDate hireDate;
}
