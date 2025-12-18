package com.warehouse.ems.employee.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String name;
    private String badgeId;
    private Set<String> roles;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
