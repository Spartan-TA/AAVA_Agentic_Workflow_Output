package com.companyname.wem.shift.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignmentDTO {
    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Shift template ID is required")
    private Long shiftTemplateId;

    @NotNull(message = "Date is required")
    private LocalDate date;
}
