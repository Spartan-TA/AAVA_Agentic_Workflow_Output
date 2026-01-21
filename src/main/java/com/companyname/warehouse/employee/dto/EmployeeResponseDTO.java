package com.companyname.warehouse.employee.dto;

import com.companyname.warehouse.employee.model.Employee;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {
    private Long id;
    private String name;
    private String badgeId;
    private Employee.Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private Employee.Status status;
}
