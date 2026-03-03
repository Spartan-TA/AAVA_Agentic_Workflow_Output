package com.wms.ems.asset.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an asset assignment to an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment extends BaseEntity {

    /**
     * The asset assigned.
     */
    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    /**
     * The employee assigned the asset.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Time the asset was checked out.
     */
    private LocalDateTime checkoutTime;

    /**
     * Time the asset was returned.
     */
    private LocalDateTime returnTime;
}