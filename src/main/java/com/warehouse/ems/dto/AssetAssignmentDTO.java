package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignmentDTO {
    private Long id;

    @NotNull
    private Long assetId;

    @NotNull
    private Long employeeId;

    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
