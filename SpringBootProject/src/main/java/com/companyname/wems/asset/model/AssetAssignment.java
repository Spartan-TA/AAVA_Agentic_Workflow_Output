package com.companyname.wems.asset.model;

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
    @Column(nullable = false)
    private Long assetId;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    @Column(nullable = false)
    private LocalDate assignedDate;

    private LocalDate returnDate;

    @NotBlank
    @Column(nullable = false)
    private String condition; // NEW, GOOD, FAIR, POOR, DAMAGED

    @NotBlank
    @Column(nullable = false)
    private String status; // ASSIGNED, RETURNED, OVERDUE
}