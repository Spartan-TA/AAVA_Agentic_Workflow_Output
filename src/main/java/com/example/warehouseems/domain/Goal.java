package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Goal JPA entity.
 */
@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotBlank
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalStatus status;

    public enum GoalType {
        PERFORMANCE, DEVELOPMENT, TRAINING, OTHER
    }

    public enum GoalStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
