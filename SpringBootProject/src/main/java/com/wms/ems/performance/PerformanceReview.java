package com.wms.ems.performance;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String period;

    @ElementCollection
    private List<Goal> goals;

    @ElementCollection
    private List<Competency> competencies;

    private String ratings;
    private String comments;

    @Column(nullable = false)
    private String status; // Draft, Submitted, Acknowledged

    @Embeddable
    public static class Goal {
        private String description;
        private boolean achieved;
    }

    @Embeddable
    public static class Competency {
        private String name;
        private String rating;
    }

    // Getters and setters omitted for brevity
}
