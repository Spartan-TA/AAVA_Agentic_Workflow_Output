package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollExportDTO {
    @NotNull
    private Long id;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @NotNull
    private LocalDate exportDate;

    @NotBlank
    @Size(max = 20)
    private String fileFormat;

    @NotBlank
    @Size(max = 50)
    private String deliveryStatus;

    @NotNull
    @Min(0)
    private Integer retryCount;
}