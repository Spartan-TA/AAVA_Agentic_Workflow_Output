package com.warehouse.employee.management.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}