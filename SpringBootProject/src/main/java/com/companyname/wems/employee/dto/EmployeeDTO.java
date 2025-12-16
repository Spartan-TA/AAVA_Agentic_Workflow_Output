package com.companyname.wems.employee.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EmployeeDTO for transferring employee data (E02)
 */
@Data
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private LocalDate hireDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
