package com.wms.scheduling.dtos;

import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for ShiftAssignment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignmentDto {
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate shiftDate;
    private boolean active;
}
