package com.warehouse.common.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for updating employee details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateDto {
    private String name;
    private Set<String> roles;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
