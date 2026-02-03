package com.company.wms.employee;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests/responses.
 */
public class EmployeeDTO {
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

    @Size(max = 20)
    private String phone;

    @NotNull
    private EmployeeRole role;

    @NotNull
    private EmployeeStatus status;

    private Long departmentId;
    private Long shiftGroupId;
    private LocalDate hireDate;
    private LocalDate terminationDate;

    // Getters and setters omitted for brevity
}
