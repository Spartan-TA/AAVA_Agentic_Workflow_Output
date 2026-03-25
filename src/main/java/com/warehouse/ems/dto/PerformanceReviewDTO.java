package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReviewDTO {
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate reviewDate;

    private String goals;
    private String comments;
    private String status;
}
