package com.warehouse.equipment.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "asset_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long assetId;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate assignedDate;

    private LocalDate returnedDate;
}
