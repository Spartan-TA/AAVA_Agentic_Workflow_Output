package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * AssetAssignment JPA entity.
 */
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull
    private LocalDate assignmentDate;

    private LocalDate returnDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetAssignmentStatus status;

    public enum AssetAssignmentStatus {
        ASSIGNED, RETURNED, OVERDUE
    }
}
