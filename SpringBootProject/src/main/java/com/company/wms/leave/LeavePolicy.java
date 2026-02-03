package com.company.wms.leave;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity representing a leave policy for a leave type.
 */
@Entity
@Table(name = "leave_policies")
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", unique = true)
    private LeaveType leaveType;

    @NotNull
    @Column(name = "max_days")
    private Double maxDays;

    @Column(name = "description")
    private String description;

    public LeavePolicy() {}

    public LeavePolicy(LeaveType leaveType, Double maxDays, String description) {
        this.leaveType = leaveType;
        this.maxDays = maxDays;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Double getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(Double maxDays) {
        this.maxDays = maxDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
