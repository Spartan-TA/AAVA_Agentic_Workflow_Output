package com.company.wms.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object for Employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate hireDate;
    private Boolean active;
    private Set<String> roles;
}
