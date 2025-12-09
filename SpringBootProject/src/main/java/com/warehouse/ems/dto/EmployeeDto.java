package com.warehouse.ems.dto;

import com.warehouse.ems.domain.Employee;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for Employee requests and responses.
 */
public class EmployeeDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Employee.Role role;
    @NotBlank
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;

    // Getters and setters omitted for brevity

    /**
     * Convert DTO to Employee entity.
     */
    public Employee toEntity() {
        Employee employee = new Employee();
        employee.setId(this.id);
        employee.setName(this.name);
        employee.setBadgeId(this.badgeId);
        employee.setRole(this.role);
        employee.setDepartment(this.department);
        employee.setShiftGroup(this.shiftGroup);
        employee.setHireDate(this.hireDate);
        employee.setStatus(this.status);
        return employee;
    }

    /**
     * Create DTO from Employee entity.
     */
    public static EmployeeDto fromEntity(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.id = employee.getId();
        dto.name = employee.getName();
        dto.badgeId = employee.getBadgeId();
        dto.role = employee.getRole();
        dto.department = employee.getDepartment();
        dto.shiftGroup = employee.getShiftGroup();
        dto.hireDate = employee.getHireDate();
        dto.status = employee.getStatus();
        return dto;
    }
}
