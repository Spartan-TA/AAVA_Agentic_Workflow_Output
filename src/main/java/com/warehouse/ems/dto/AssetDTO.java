package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDTO {
    private Long id;

    @NotBlank
    private String type;

    @NotBlank
    private String serialNumber;

    private String condition;
}
