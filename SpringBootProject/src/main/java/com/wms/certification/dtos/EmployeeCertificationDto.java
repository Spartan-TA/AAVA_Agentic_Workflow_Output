package com.wms.certification.dtos;

import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for EmployeeCertification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCertificationDto {
    private Long id;
    private Long employeeId;
    private Long certificationId;
    private LocalDate obtainedDate;
    private LocalDate expiryDate;
    private boolean active;
}
