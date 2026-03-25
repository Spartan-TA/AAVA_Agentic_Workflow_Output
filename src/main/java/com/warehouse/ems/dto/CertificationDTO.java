package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDTO {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @NotNull
    private Long employeeId;

    private String documentUrl;
}
