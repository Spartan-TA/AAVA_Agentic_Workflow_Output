package com.wms.asset.dtos;

import lombok.*;

/**
 * Data Transfer Object for Asset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDto {
    private Long id;
    private String name;
    private String type;
    private String serialNumber;
    private boolean active;
}
