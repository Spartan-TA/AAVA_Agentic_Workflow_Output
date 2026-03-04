package com.warehouse.ems.leave;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Entity
@Table(name = "leave_request")
@Schema(description = "Leave request entity for employee leave management")
public class LeaveRequest extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Leave request ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @Schema(description = "Employee requesting leave")
    private Employee employee;

    @NotBlank
    @Schema(description = "Leave type (PTO/SICK/UNPAID)")
    private String type;

    @NotNull
    @Schema(description = "Leave start date")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "Leave end date")
    private LocalDate endDate;

    @NotBlank
    @Schema(description = "Leave status (REQUESTED, APPROVED, REJECTED)")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    @Schema(description = "Approver for the leave request")
    private Employee approver;

    @Schema(description = "Leave balance at time of request")
    private Double balance;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Employee getApprover() { return approver; }
    public void setApprover(Employee approver) { this.approver = approver; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}
