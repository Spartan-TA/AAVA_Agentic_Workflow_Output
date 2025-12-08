package com.company.wms.employee.dto;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Employee API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    @NotBlank
    private String shiftGroup;
    @PastOrPresent
    private LocalDate hireDate;
    @NotBlank
    private String status;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
