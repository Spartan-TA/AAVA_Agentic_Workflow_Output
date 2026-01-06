package com.company.wms.performance.model;

import javax.persistence.*;

/**
 * Entity representing a goal within a performance review.
 */
@Entity
@Table(name = "review_goals")
public class ReviewGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The performance review this goal belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_review_id", nullable = false)
    private PerformanceReview performanceReview;

    /**
     * Description of the goal.
     */
    @Column(nullable = false, length = 1000)
    private String description;

    /**
     * Status of the goal (e.g., PENDING, COMPLETED).
     */
    @Column(nullable = false)
    private String status;

    // Constructors, getters, setters, equals, hashCode, toString

    public ReviewGoal() {}

    public ReviewGoal(PerformanceReview performanceReview, String description, String status) {
        this.performanceReview = performanceReview;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PerformanceReview getPerformanceReview() {
        return performanceReview;
    }

    public void setPerformanceReview(PerformanceReview performanceReview) {
        this.performanceReview = performanceReview;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewGoal that = (ReviewGoal) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ReviewGoal{" +
                "id=" + id +
                ", performanceReview=" + (performanceReview != null ? performanceReview.getId() : null) +
                ", description='" + description + ''' +
                ", status='" + status + ''' +
                '}';
    }
}
