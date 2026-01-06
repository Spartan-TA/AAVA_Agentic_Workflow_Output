package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignmentDTO {
    @NotNull
    private Long assetId;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate checkoutDate;
}