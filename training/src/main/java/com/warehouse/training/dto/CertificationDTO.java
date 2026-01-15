package com.warehouse.training.dto;

import com.warehouse.training.entity.Certification;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDTO {
    private Long id;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Certification.Status status;
    private Long employeeId;
}
