package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignmentDTO {
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private Long shiftTemplateId;

    @NotNull
    private LocalDate date;
}
