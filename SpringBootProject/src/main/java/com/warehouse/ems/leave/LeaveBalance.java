package com.warehouse.ems.leave;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "leave_balance")
@Schema(description = "Leave balance entity for accrual tracking")
public class LeaveBalance extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Leave balance ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @Schema(description = "Employee for whom leave balance is tracked")
    private Employee employee;

    @NotBlank
    @Schema(description = "Leave type (PTO/SICK/UNPAID)")
    private String type;

    @NotNull
    @Schema(description = "Current leave balance")
    private Double balance;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}
