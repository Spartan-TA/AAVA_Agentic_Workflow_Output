package com.wms.asset.dtos;

import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for AssetAssignment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignmentDto {
    private Long id;
    private Long employeeId;
    private Long assetId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}
