package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.util.List;

public class PerformanceReviewDto {
    @NotNull
    private Long employeeId;

    @NotBlank
    private String period;

    @NotNull
    @Size(min = 1)
    private List<String> goals;

    @Min(1)
    @Max(5)
    private Integer rating;

    @NotNull
    private Long reviewerId;

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<String> getGoals() { return goals; }
    public void setGoals(List<String> goals) { this.goals = goals; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
}
