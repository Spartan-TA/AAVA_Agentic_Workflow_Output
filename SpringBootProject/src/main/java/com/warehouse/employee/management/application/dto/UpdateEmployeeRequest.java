package com.warehouse.employee.management.application.dto;

import com.warehouse.employee.management.domain.employee.Address;
import com.warehouse.employee.management.domain.employee.EmergencyContact;
import com.warehouse.employee.management.domain.employee.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateEmployeeRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    private String phone;

    private LocalDate hireDate;
    private LocalDate terminationDate;

    private EmployeeStatus status;
    private UUID departmentId;
    private UUID positionId;
    private UUID supervisorId;
    private Address address;
    private EmergencyContact emergencyContact;
}
