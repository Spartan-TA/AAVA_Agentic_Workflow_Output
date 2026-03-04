package com.warehouse.ems.leave;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "DTO for LeaveRequest and LeaveBalance entities")
public class LeaveDTO {
    @Schema(description = "Leave request ID")
    private Long id;

    @Schema(description = "Employee ID")
    private Long employeeId;

    @Schema(description = "Leave type (PTO/SICK/UNPAID)")
    private String type;

    @Schema(description = "Leave start date")
    private LocalDate startDate;

    @Schema(description = "Leave end date")
    private LocalDate endDate;

    @Schema(description = "Leave status (REQUESTED, APPROVED, REJECTED)")
    private String status;

    @Schema(description = "Approver ID")
    private Long approverId;

    @Schema(description = "Leave balance at time of request")
    private Double balance;

    // Getters and setters
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}
