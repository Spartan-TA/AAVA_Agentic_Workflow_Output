package com.wms.ems.asset.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.common.AssetCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity representing an asset in the warehouse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assets")
public class Asset extends BaseEntity {

    /**
     * Type of the asset.
     */
    @Column(nullable = false)
    private String type;

    /**
     * Serial number of the asset.
     */
    @Column(unique = true)
    private String serialNumber;

    /**
     * Condition of the asset.
     */
    @Enumerated(EnumType.STRING)
    private AssetCondition condition;
}