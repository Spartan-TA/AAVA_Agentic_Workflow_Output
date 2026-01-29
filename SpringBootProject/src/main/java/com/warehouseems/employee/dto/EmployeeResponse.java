package com.warehouseems.employee.dto;

import com.warehouseems.employee.Role;
import com.warehouseems.employee.Status;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String name;
    private String badgeId;
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
