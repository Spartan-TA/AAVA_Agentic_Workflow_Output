package com.warehouse.ems.dto;

import java.time.LocalDate;

public class LeaveRequestDTO {
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private double balance;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
