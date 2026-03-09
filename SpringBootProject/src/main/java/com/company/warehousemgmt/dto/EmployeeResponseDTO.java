package com.company.warehousemgmt.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO for employee API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {
    private Long id;
    private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private String roleName;
}
