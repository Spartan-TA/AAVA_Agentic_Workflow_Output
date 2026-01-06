package com.company.wms.performance.model;

import com.company.wms.employee.model.Employee;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity representing a performance review for an employee.
 */
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee being reviewed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * The reviewer (manager or supervisor).
     */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /**
     * Date of the review.
     */
    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    /**
     * Overall comments for the review.
     */
    @Column(length = 2000)
    private String comments;

    /**
     * List of goals for this review (one-to-many relationship).
     */
    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewGoal> goals;

    // Constructors, getters, setters, equals, hashCode, toString

    public PerformanceReview() {}

    public PerformanceReview(Employee employee, Long reviewerId, LocalDate reviewDate, String comments) {
        this.employee = employee;
        this.reviewerId = reviewerId;
        this.reviewDate = reviewDate;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<ReviewGoal> getGoals() {
        return goals;
    }

    public void setGoals(List<ReviewGoal> goals) {
        this.goals = goals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceReview that = (PerformanceReview) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "PerformanceReview{" +
                "id=" + id +
                ", employee=" + (employee != null ? employee.getId() : null) +
                ", reviewerId=" + reviewerId +
                ", reviewDate=" + reviewDate +
                ", comments='" + comments + ''' +
                '}';
    }
}
