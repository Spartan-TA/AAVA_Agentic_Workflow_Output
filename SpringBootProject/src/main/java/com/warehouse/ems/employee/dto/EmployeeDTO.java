package com.warehouse.ems.employee.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private Long departmentId;
    private String departmentName;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
