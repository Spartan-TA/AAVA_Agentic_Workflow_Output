package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String reportType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Size(max = 100)
    private String filterBy;

    @Size(max = 100)
    private String exportFormat;

    private List<@NotBlank String> additionalFields;
}