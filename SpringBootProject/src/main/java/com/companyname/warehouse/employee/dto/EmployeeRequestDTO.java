package com.companyname.warehouse.employee.dto;

import com.companyname.warehouse.common.enums.Role;
import com.companyname.warehouse.common.enums.Status;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for employee creation and update requests.
 */
@Data
public class EmployeeRequestDTO {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotNull
    private Role role;
    @NotNull
    private Status status;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String address;
    private String emergencyContact;
}
