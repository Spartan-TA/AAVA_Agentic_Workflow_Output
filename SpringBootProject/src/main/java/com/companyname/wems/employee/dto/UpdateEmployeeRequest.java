package com.companyname.wems.employee.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for updating employee details (E02)
 */
@Data
public class UpdateEmployeeRequest {
    @Size(min = 1, max = 50)
    private String firstName;

    @Size(min = 1, max = 50)
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 50)
    private String department;

    @Size(max = 50)
    private String position;

    private LocalDate hireDate;

    @Size(max = 20)
    private String status;
}
