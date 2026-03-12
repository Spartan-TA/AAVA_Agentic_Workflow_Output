package com.warehouse.management.employee.dto;

import com.warehouse.management.employee.Employee;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for Employee API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dob;
    private LocalDate hireDate;
    private String status;
    private String department;
    private String position;
    private Set<String> roles;

    public static EmployeeResponse fromEntity(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .dob(employee.getDob())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .roles(employee.getRoles() != null ? employee.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()) : null)
                .build();
    }
}
