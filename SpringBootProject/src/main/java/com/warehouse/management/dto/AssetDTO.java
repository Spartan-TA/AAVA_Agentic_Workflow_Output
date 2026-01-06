package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDTO {
    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String assetType;

    @NotBlank
    @Size(max = 100)
    private String serialNumber;

    @NotBlank
    @Size(max = 50)
    private String condition;

    private Long assignedToEmployeeId;

    private LocalDate checkoutDate;

    private LocalDate returnDate;
}