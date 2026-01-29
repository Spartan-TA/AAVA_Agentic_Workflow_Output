package com.company.wms.employee.dto;

import com.company.wms.employee.model.Employee;
import com.company.wms.common.model.Role;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Employee entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeDTO fromEntity(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public Employee toEntity() {
        return Employee.builder()
                .id(id)
                .badgeId(badgeId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(role)
                .department(department)
                .shiftGroup(shiftGroup)
                .hireDate(hireDate)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
