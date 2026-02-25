package com.warehouse.employee.management.application.dto;

import com.warehouse.employee.management.domain.employee.Address;
import com.warehouse.employee.management.domain.employee.Department;
import com.warehouse.employee.management.domain.employee.EmergencyContact;
import com.warehouse.employee.management.domain.employee.EmployeeStatus;
import com.warehouse.employee.management.domain.employee.Position;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeResponse {
    private UUID id;
    private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private EmployeeStatus status;
    private Department department;
    private Position position;
    private UUID supervisorId;
    private Address address;
    private EmergencyContact emergencyContact;
    private String tenantId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String lastModifiedBy;
    private Integer version;
    private boolean deleted;
}
