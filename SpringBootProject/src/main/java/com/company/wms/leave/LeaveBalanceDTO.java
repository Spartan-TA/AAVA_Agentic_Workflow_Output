package com.company.wms.leave;

import javax.validation.constraints.NotNull;

/**
 * Data Transfer Object for Leave Balance information.
 */
public class LeaveBalanceDTO {

    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private LeaveType leaveType;

    @NotNull
    private Double balance;

    public LeaveBalanceDTO() {}

    public LeaveBalanceDTO(Long id, Long employeeId, LeaveType leaveType, Double balance) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
