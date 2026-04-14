package com.wms.ems.entity;

import com.wms.ems.entity.enums.LeaveType;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an employee's leave balance.
 */
@Entity
@Table(name = "leave_balance")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Double accruedHours;

    @Column(nullable = false)
    private Double usedHours;

    @Column(nullable = false)
    private Double balanceHours;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public Double getAccruedHours() { return accruedHours; }
    public void setAccruedHours(Double accruedHours) { this.accruedHours = accruedHours; }
    public Double getUsedHours() { return usedHours; }
    public void setUsedHours(Double usedHours) { this.usedHours = usedHours; }
    public Double getBalanceHours() { return balanceHours; }
    public void setBalanceHours(Double balanceHours) { this.balanceHours = balanceHours; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
