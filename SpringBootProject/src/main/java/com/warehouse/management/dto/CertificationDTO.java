package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long employeeId;

    @NotBlank
    @Size(max = 100)
    private String certType;

    @NotNull
    private LocalDate issueDate;

    @NotNull
    private LocalDate expiryDate;

    @Size(max = 255)
    private String proofDocument;

    @NotBlank
    @Size(max = 50)
    private String status;
}