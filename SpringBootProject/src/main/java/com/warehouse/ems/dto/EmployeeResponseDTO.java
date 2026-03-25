package com.warehouse.ems.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {
    private Long id;
    private String badgeId;
    private String name;
    private Long roleId;
    private String roleName;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
