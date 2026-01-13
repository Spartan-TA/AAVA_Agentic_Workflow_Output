package com.warehouse.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_exports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String period; // e.g., 2024-01

    @NotBlank
    @Size(max = 20)
    private String status; // PENDING, COMPLETED, FAILED

    @Size(max = 255)
    private String filePath;

    @NotBlank
    @Size(max = 20)
    private String deliveryStatus; // DELIVERED, FAILED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
