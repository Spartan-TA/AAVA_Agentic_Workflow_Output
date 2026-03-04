package com.warehouse.ems.scheduling;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Entity
@Table(name = "schedule")
@Schema(description = "Schedule entity for employee shift assignments")
public class Schedule extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Schedule ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @Schema(description = "Employee assigned to this schedule")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    @Schema(description = "Shift template for this schedule")
    private ShiftTemplate shift;

    @NotNull
    @Schema(description = "Date of the schedule")
    private LocalDate date;

    @NotBlank
    @Schema(description = "Schedule status (ASSIGNED, CONFLICT, COMPLETED)")
    private String status;

    @Schema(description = "Conflict description if any")
    private String conflict;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public ShiftTemplate getShift() { return shift; }
    public void setShift(ShiftTemplate shift) { this.shift = shift; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConflict() { return conflict; }
    public void setConflict(String conflict) { this.conflict = conflict; }
}
