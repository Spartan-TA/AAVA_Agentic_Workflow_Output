package com.wms.optimization.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing a schedule recommendation from the optimization engine.
 */
@Data
@Entity
@Table(name = "schedule_recommendations")
public class ScheduleRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Shift ID being recommended */
    @Column(nullable = false)
    private Long shiftId;

    /** Employee ID being recommended for the shift */
    @Column(nullable = false)
    private Long employeeId;

    /** Recommendation score (e.g., ML confidence) */
    @Column(nullable = false)
    private double score;

    /** Reasoning or explanation for the recommendation */
    @Lob
    private String reasoning;
}
