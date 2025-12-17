package com.warehouse.employee.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ShiftAssignment entity for assigning shifts to employees.
 * Includes audit fields and validation.
 */
@Entity
@Table(name = "shift_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull
    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @NotNull
    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
