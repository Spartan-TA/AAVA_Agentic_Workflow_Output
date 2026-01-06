package com.companyname.warehouse.employee.dto;

import com.companyname.warehouse.common.enums.Role;
import com.companyname.warehouse.common.enums.Status;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for employee API responses.
 */
@Data
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Status status;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String address;
    private String emergencyContact;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
