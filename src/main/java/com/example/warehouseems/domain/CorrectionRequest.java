package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * CorrectionRequest JPA entity.
 */
@Entity
@Table(name = "correction_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull
    private LocalDateTime requestDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CorrectionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CorrectionStatus status;

    private String description;

    public enum CorrectionType {
        ATTENDANCE, LEAVE, SHIFT, OTHER
    }

    public enum CorrectionStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
