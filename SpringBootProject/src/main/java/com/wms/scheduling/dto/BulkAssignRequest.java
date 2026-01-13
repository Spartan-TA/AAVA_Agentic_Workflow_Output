package com.wms.scheduling.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for bulk shift assignment requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignRequest {
    @NotEmpty
    private List<Long> employeeIds;
    @NotNull
    private Long shiftTemplateId;
    @NotNull
    private LocalDate date;
}
