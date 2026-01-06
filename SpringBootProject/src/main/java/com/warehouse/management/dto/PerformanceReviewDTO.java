package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReviewDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private Long reviewerId;

    @NotNull
    private Long templateId;

    @NotNull
    private LocalDate reviewDate;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> goals;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> competencies;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> ratings;

    @Size(max = 1000)
    private String comments;

    @NotBlank
    @Size(max = 50)
    private String status;

    private LocalDate acknowledgedDate;
}